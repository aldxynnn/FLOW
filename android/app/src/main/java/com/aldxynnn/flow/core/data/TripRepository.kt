package com.aldxynnn.flow.core.data

import com.aldxynnn.flow.core.network.ApiService
import com.aldxynnn.flow.core.network.LoginRequest
import com.aldxynnn.flow.core.network.LoginResponse
import com.aldxynnn.flow.core.network.Trip
import com.aldxynnn.flow.core.session.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import retrofit2.HttpException
import com.google.gson.Gson
import android.content.Context
import com.aldxynnn.flow.core.work.SyncWorker

class TripRepository(
    private val api: ApiService,
    private val session: SessionStore,
    private val queue: ActionQueue,
    private val context: Context
) {
    suspend fun login(username: String, password: String): LoginResponse = withContext(Dispatchers.IO) {
        api.login(LoginRequest(username.trim(), password))
    }

    fun enqueuePendingSync() {
        SyncWorker.enqueue(context)
    }

    suspend fun trips(): Result<List<Trip>> = withContext(Dispatchers.IO) {
        runCatching {
            api.trips().also { session.saveTrips(it) }
        }.recoverCatching { error ->
            if (error is IOException) {
                session.cachedTrips()
            } else if (error is HttpException && error.code() == 401) {
                session.clearAuthentication()
                throw error
            } else {
                throw error
            }
        }
    }

    suspend fun startTrip(trip: Trip): ActionResult = performStatusAction(
        tripId = trip.id,
        apiCall = { api.startTrip(trip.id) },
        offlineAction = QueuedAction("START", trip.id),
        localStatus = "IN_PROGRESS"
    )

    suspend fun completeTrip(trip: Trip): ActionResult = performStatusAction(
        tripId = trip.id,
        apiCall = { api.completeTrip(trip.id) },
        offlineAction = QueuedAction("COMPLETE", trip.id),
        localStatus = "COMPLETED"
    )

    suspend fun createTrip(request: com.aldxynnn.flow.core.network.CreateTripRequest) = withContext(Dispatchers.IO) { runCatching { api.createTrip(request) }.recoverCatching { if (it is HttpException) throw httpMessage(it) else throw it } }
    suspend fun assignTrip(id: Long, driverUsername: String) = withContext(Dispatchers.IO) { runCatching { api.assignTrip(id, com.aldxynnn.flow.core.network.AssignTripRequest(driverUsername)) }.recoverCatching { if (it is HttpException) throw httpMessage(it) else throw it } }
    suspend fun dashboardSummary() = withContext(Dispatchers.IO) { runCatching { api.dashboardSummary() } }
    suspend fun drivers() = withContext(Dispatchers.IO) { runCatching { api.drivers() } }
    suspend fun vehicles() = withContext(Dispatchers.IO) { runCatching { api.vehicles() } }
    suspend fun createVehicle(request: com.aldxynnn.flow.core.network.VehicleRequest) = withContext(Dispatchers.IO) { runCatching { api.createVehicle(request) }.recoverCatching { if (it is HttpException) throw httpMessage(it) else throw it } }
    suspend fun users() = withContext(Dispatchers.IO) { runCatching { api.users() } }
    suspend fun createUser(request: com.aldxynnn.flow.core.network.CreateUserRequest) = withContext(Dispatchers.IO) { runCatching { api.createUser(request) }.recoverCatching { if (it is HttpException) throw httpMessage(it) else throw it } }
    suspend fun setUserEnabled(id: Long, enabled: Boolean) = withContext(Dispatchers.IO) { runCatching { api.setUserEnabled(id, com.aldxynnn.flow.core.network.SetUserEnabledRequest(enabled)) }.recoverCatching { if (it is HttpException) throw httpMessage(it) else throw it } }

    suspend fun updateLocation(id: Long, latitude: Double, longitude: Double) = withContext(Dispatchers.IO) {
        try {
            val updated = api.updateLocation(id, com.aldxynnn.flow.core.network.LocationRequest(latitude, longitude))
            session.updateCachedTrip(updated)
        } catch (_: IOException) {
            queue.enqueue(QueuedAction("LOCATION", id, latitude, longitude))
            SyncWorker.enqueue(context)
        } catch (error: HttpException) {
            if (error.code() in 500..599 || error.code() == 429) {
                queue.enqueue(QueuedAction("LOCATION", id, latitude, longitude))
                SyncWorker.enqueue(context)
            }
        }
    }

    private fun httpMessage(error: HttpException): Exception {
        val raw = error.response()?.errorBody()?.string().orEmpty()
        val message = runCatching {
            Gson().fromJson(raw, com.aldxynnn.flow.core.network.ApiError::class.java)?.message
        }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: "Server menolak permintaan (${error.code()})"
        return IllegalStateException(message)
    }

    private suspend fun performStatusAction(
        tripId: Long,
        apiCall: suspend () -> Trip,
        offlineAction: QueuedAction,
        localStatus: String
    ): ActionResult = withContext(Dispatchers.IO) {
        try {
            val updated = apiCall()
            session.updateCachedTrip(updated)
            ActionResult.Applied(updated)
        } catch (error: IOException) {
            queue.enqueue(offlineAction)
            SyncWorker.enqueue(context)
            val cached = session.cachedTrips().firstOrNull { it.id == tripId }
            if (cached != null) {
                val local = cached.copy(status = localStatus)
                session.updateCachedTrip(local)
                ActionResult.Queued(local)
            } else {
                ActionResult.Queued(null)
            }
        } catch (error: HttpException) {
            ActionResult.Failed(error.code())
        } catch (error: Exception) {
            ActionResult.Failed(null)
        }
    }
}

enum class ActionResultType { APPLIED, QUEUED, FAILED }
sealed interface ActionResult {
    val type: ActionResultType
    data class Applied(val trip: Trip): ActionResult { override val type = ActionResultType.APPLIED }
    data class Queued(val trip: Trip?): ActionResult { override val type = ActionResultType.QUEUED }
    data class Failed(val code: Int?): ActionResult { override val type = ActionResultType.FAILED }
}
