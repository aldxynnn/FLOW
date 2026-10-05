package com.aldxynnn.flow.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import com.aldxynnn.flow.FlowApplication
import com.aldxynnn.flow.core.data.QueuedAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import retrofit2.HttpException

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val app = applicationContext as FlowApplication
        val actions = app.actionQueue.takeAll()
        if (actions.isEmpty()) return@withContext Result.success()

        val failed = mutableListOf<QueuedAction>()
        var sessionExpired = false
        for (action in actions) {
            try {
                when (action.type) {
                    "START" -> app.apiProvider.service.startTrip(action.tripId).also { app.sessionStore.updateCachedTrip(it) }
                    "COMPLETE" -> app.apiProvider.service.completeTrip(action.tripId).also { app.sessionStore.updateCachedTrip(it) }
                    "LOCATION" -> {
                        val lat = action.latitude ?: return@withContext Result.failure()
                        val lon = action.longitude ?: return@withContext Result.failure()
                        app.apiProvider.service.updateLocation(action.tripId, com.aldxynnn.flow.core.network.LocationRequest(lat, lon))
                            .also { app.sessionStore.updateCachedTrip(it) }
                    }
                }
            } catch (_: IOException) {
                failed += action.copy(attempts = action.attempts + 1)
            } catch (error: HttpException) {
                when (SyncPolicy.forHttpStatus(error.code())) {
                    SyncPolicy.Decision.CLEAR_SESSION_AND_RETAIN -> {
                        app.sessionStore.clearAuthentication()
                        failed += action
                        sessionExpired = true
                        break
                    }
                    SyncPolicy.Decision.RETRY -> failed += action.copy(attempts = action.attempts + 1)
                    SyncPolicy.Decision.DROP -> Unit
                }
            }
        }

        if (failed.isNotEmpty()) {
            app.actionQueue.restore(failed)
            if (sessionExpired) Result.failure() else Result.retry()
        } else {
            Result.success()
        }
    }

    companion object {
        private const val UNIQUE_NAME = "flow-offline-sync"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
