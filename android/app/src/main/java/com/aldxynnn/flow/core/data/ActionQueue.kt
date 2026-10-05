package com.aldxynnn.flow.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.withLock

private val Context.flowQueueDataStore by preferencesDataStore("flow_queue")

class ActionQueue(private val context: Context) {
    private val mutex = Mutex()
    private val gson = Gson()
    private val key = stringPreferencesKey("actions")

    private suspend fun read(): MutableList<QueuedAction> {
        val prefs = context.flowQueueDataStore.data.first()
        val json = prefs[key] ?: return mutableListOf()
        val type = object : TypeToken<List<QueuedAction>>() {}.type
        return runCatching { gson.fromJson<List<QueuedAction>>(json, type)?.toMutableList() ?: mutableListOf() }
            .getOrDefault(mutableListOf())
    }

    suspend fun enqueue(action: QueuedAction) = mutex.withLock {
        val actions = read()
        if (action.type == "LOCATION") {
            actions.removeAll { it.type == "LOCATION" && it.tripId == action.tripId && it.createdAtEpochMs < action.createdAtEpochMs - 60_000L }
            actions += action
            val locations = actions.filter { it.type == "LOCATION" }.sortedBy { it.createdAtEpochMs }
            if (locations.size > MAX_LOCATION_POINTS) {
                val keep = locations.takeLast(MAX_LOCATION_POINTS).toSet()
                actions.removeAll { it.type == "LOCATION" && it !in keep }
            }
        } else {
            actions.removeAll { it.type == action.type && it.tripId == action.tripId }
            actions += action
        }
        context.flowQueueDataStore.edit { it[key] = gson.toJson(actions.sortedBy { a -> a.createdAtEpochMs }) }
    }

    suspend fun takeAll(): List<QueuedAction> = mutex.withLock {
        val actions = read()
        context.flowQueueDataStore.edit { it.remove(key) }
        actions
    }

    suspend fun restore(actions: List<QueuedAction>) = mutex.withLock {
        if (actions.isEmpty()) return@withLock
        val existing = read()
        val merged = (existing + actions)
            .groupBy { if (it.type == "LOCATION") "LOCATION:${it.tripId}:${it.createdAtEpochMs}" else "${it.type}:${it.tripId}" }
            .values
            .map { it.maxBy { action -> action.createdAtEpochMs } }
            .sortedBy { it.createdAtEpochMs }
        context.flowQueueDataStore.edit { it[key] = gson.toJson(merged) }
    }

    companion object {
        private const val MAX_LOCATION_POINTS = 300
    }
}
