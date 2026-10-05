package com.aldxynnn.flow.core.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aldxynnn.flow.core.network.Trip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.flowDataStore by preferencesDataStore("flow_session")

class SessionStore(private val context: Context) {
    private val gson = Gson()
    private val tokenKey = stringPreferencesKey("token")
    private val usernameKey = stringPreferencesKey("username")
    private val roleKey = stringPreferencesKey("role")
    private val cachedTripsKey = stringPreferencesKey("cached_trips")

    val session: Flow<Session?> = context.flowDataStore.data.map { prefs ->
        val token = prefs[tokenKey] ?: return@map null
        Session(
            token = token,
            username = prefs[usernameKey].orEmpty(),
            role = prefs[roleKey].orEmpty()
        )
    }

    suspend fun saveLogin(response: com.aldxynnn.flow.core.network.LoginResponse) {
        context.flowDataStore.edit { prefs ->
            prefs[tokenKey] = response.accessToken
            prefs[usernameKey] = response.username
            prefs[roleKey] = response.role
        }
    }

    suspend fun clear() {
        context.flowDataStore.edit { it.clear() }
    }

    suspend fun clearAuthentication() {
        context.flowDataStore.edit { prefs ->
            prefs.remove(tokenKey)
            prefs.remove(usernameKey)
            prefs.remove(roleKey)
        }
    }

    suspend fun token(): String? = context.flowDataStore.data.first()[tokenKey]

    suspend fun currentSession(): Session? = session.first()

    suspend fun saveTrips(trips: List<Trip>) {
        context.flowDataStore.edit { prefs ->
            prefs[cachedTripsKey] = gson.toJson(trips)
        }
    }

    suspend fun cachedTrips(): List<Trip> {
        val json = context.flowDataStore.data.first()[cachedTripsKey] ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<Trip>>() {}.type
            gson.fromJson<List<Trip>>(json, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    suspend fun updateCachedTrip(updated: Trip) {
        val current = cachedTrips().toMutableList()
        val index = current.indexOfFirst { it.id == updated.id }
        if (index >= 0) current[index] = updated else current.add(0, updated)
        saveTrips(current)
    }
}

data class Session(val token: String, val username: String, val role: String)
