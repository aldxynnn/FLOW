package com.aldxynnn.flow.core.data

import android.content.Context
import com.aldxynnn.flow.core.network.ApiService
import com.aldxynnn.flow.core.network.LoginRequest
import com.aldxynnn.flow.core.network.LoginResponse
import com.aldxynnn.flow.core.network.Trip
import com.aldxynnn.flow.core.network.TripLocation
import com.aldxynnn.flow.core.session.SessionStore
import com.aldxynnn.flow.core.work.SyncWorker
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class TripRepository(
    private val api: ApiService,
    private val session: SessionStore,
    private val queue: ActionQueue,
    private val context: Context
) {

    private val _locationUpdates =
        MutableSharedFlow<LocalLocationUpdate>(
            extraBufferCapacity = 64
        )

    val locationUpdates:
            SharedFlow<LocalLocationUpdate> =
        _locationUpdates.asSharedFlow()

    suspend fun login(
        username: String,
        password: String
    ): LoginResponse =
        withContext(Dispatchers.IO) {
            api.login(
                LoginRequest(
                    username.trim(),
                    password
                )
            )
        }

    fun enqueuePendingSync() {
        SyncWorker.enqueue(context)
    }

    suspend fun trips():
            Result<List<Trip>> =
        withContext(Dispatchers.IO) {

            runCatching {
                api.trips()
                    .also {
                        session.saveTrips(it)
                    }
            }.recoverCatching { error ->

                when {

                    error is IOException -> {
                        session.cachedTrips()
                    }

                    error is HttpException &&
                            error.code() == 401 -> {
                        session.clearAuthentication()
                        throw error
                    }

                    else -> {
                        throw error
                    }
                }
            }
        }

    suspend fun startTrip(
        trip: Trip
    ): ActionResult =
        performStatusAction(
            tripId = trip.id,
            apiCall = {
                api.startTrip(
                    trip.id
                )
            },
            offlineAction =
                QueuedAction(
                    "START",
                    trip.id
                ),
            localStatus = "IN_PROGRESS"
        )

    suspend fun completeTrip(
        trip: Trip
    ): ActionResult =
        performStatusAction(
            tripId = trip.id,
            apiCall = {
                api.completeTrip(
                    trip.id
                )
            },
            offlineAction =
                QueuedAction(
                    "COMPLETE",
                    trip.id
                ),
            localStatus = "COMPLETED"
        )

    suspend fun createTrip(
        request:
        com.aldxynnn.flow.core.network.CreateTripRequest
    ) =
        withContext(Dispatchers.IO) {

            runCatching {
                api.createTrip(
                    request
                )
            }.recoverCatching {
                if (it is HttpException) {
                    throw httpMessage(it)
                } else {
                    throw it
                }
            }
        }

    suspend fun assignTrip(
        id: Long,
        driverUsername: String
    ) =
        withContext(Dispatchers.IO) {

            runCatching {
                api.assignTrip(
                    id,
                    com.aldxynnn.flow.core.network.AssignTripRequest(
                        driverUsername
                    )
                )
            }.recoverCatching {
                if (it is HttpException) {
                    throw httpMessage(it)
                } else {
                    throw it
                }
            }
        }

    suspend fun dashboardSummary() =
        withContext(Dispatchers.IO) {
            runCatching {
                api.dashboardSummary()
            }
        }

    suspend fun drivers() =
        withContext(Dispatchers.IO) {
            runCatching {
                api.drivers()
            }
        }

    suspend fun vehicles() =
        withContext(Dispatchers.IO) {
            runCatching {
                api.vehicles()
            }
        }

    suspend fun createVehicle(
        request:
        com.aldxynnn.flow.core.network.VehicleRequest
    ) =
        withContext(Dispatchers.IO) {

            runCatching {
                api.createVehicle(
                    request
                )
            }.recoverCatching {
                if (it is HttpException) {
                    throw httpMessage(it)
                } else {
                    throw it
                }
            }
        }

    suspend fun users() =
        withContext(Dispatchers.IO) {
            runCatching {
                api.users()
            }
        }

    suspend fun createUser(
        request:
        com.aldxynnn.flow.core.network.CreateUserRequest
    ) =
        withContext(Dispatchers.IO) {

            runCatching {
                api.createUser(
                    request
                )
            }.recoverCatching {
                if (it is HttpException) {
                    throw httpMessage(it)
                } else {
                    throw it
                }
            }
        }

    suspend fun setUserEnabled(
        id: Long,
        enabled: Boolean
    ) =
        withContext(Dispatchers.IO) {

            runCatching {
                api.setUserEnabled(
                    id,
                    com.aldxynnn.flow.core.network.SetUserEnabledRequest(
                        enabled
                    )
                )
            }.recoverCatching {
                if (it is HttpException) {
                    throw httpMessage(it)
                } else {
                    throw it
                }
            }
        }

    suspend fun tripLocations(
        id: Long
    ): Result<List<TripLocation>> =
        withContext(Dispatchers.IO) {

            runCatching {
                api.tripLocations(
                    id
                )
            }
        }

    suspend fun updateLocation(
        id: Long,
        latitude: Double,
        longitude: Double
    ): Result<Unit> =
        withContext(Dispatchers.IO) {

            runCatching<Unit> {

                require(
                    latitude in -90.0..90.0
                ) {
                    "Latitude di luar range valid."
                }

                require(
                    longitude in -180.0..180.0
                ) {
                    "Longitude di luar range valid."
                }

                try {

                    val updated =
                        api.updateLocation(
                            id,
                            com.aldxynnn.flow.core.network.LocationRequest(
                                latitude,
                                longitude
                            )
                        )

                    session.updateCachedTrip(
                        updated
                    )

                    _locationUpdates.tryEmit(
                        LocalLocationUpdate(
                            id,
                            latitude,
                            longitude
                        )
                    )

                    Unit

                } catch (_: IOException) {

                    queue.enqueue(
                        QueuedAction(
                            "LOCATION",
                            id,
                            latitude,
                            longitude
                        )
                    )

                    SyncWorker.enqueue(
                        context
                    )

                    applyLocalLocation(
                        id,
                        latitude,
                        longitude
                    )

                } catch (error: HttpException) {

                    when {

                        error.code() in
                                500..599 ||
                                error.code() == 429 -> {

                            queue.enqueue(
                                QueuedAction(
                                    "LOCATION",
                                    id,
                                    latitude,
                                    longitude
                                )
                            )

                            SyncWorker.enqueue(
                                context
                            )

                            applyLocalLocation(
                                id,
                                latitude,
                                longitude
                            )
                        }

                        error.code() == 401 -> {
                            session.clearAuthentication()
                            throw httpMessage(
                                error
                            )
                        }

                        else -> {
                            throw httpMessage(
                                error
                            )
                        }
                    }
                }
            }
        }

    private suspend fun applyLocalLocation(
        id: Long,
        latitude: Double,
        longitude: Double
    ) {
        session
            .cachedTrips()
            .firstOrNull {
                it.id == id
            }
            ?.let { cached ->

                session.updateCachedTrip(
                    cached.copy(
                        latitude = latitude,
                        longitude = longitude,
                        lastLocationAt =
                            java.time.Instant
                                .now()
                                .toString()
                    )
                )
            }

        _locationUpdates.tryEmit(
            LocalLocationUpdate(
                id,
                latitude,
                longitude
            )
        )
    }

    private fun httpMessage(
        error: HttpException
    ): Exception {

        val raw =
            error.response()
                ?.errorBody()
                ?.string()
                ?.trim()
                .orEmpty()

        val parsedMessage =
            runCatching {
                Gson()
                    .fromJson(
                        raw,
                        com.aldxynnn.flow.core.network.ApiError::class.java
                    )
                    ?.message
            }.getOrNull()
                ?.takeIf {
                    it.isNotBlank()
                }

        val message =
            parsedMessage
                ?: raw.takeIf {
                    it.isNotBlank()
                }
                ?: "Server menolak permintaan (${error.code()})"

        return IllegalStateException(
            message
        )
    }

    private suspend fun performStatusAction(
        tripId: Long,
        apiCall: suspend () -> Trip,
        offlineAction: QueuedAction,
        localStatus: String
    ): ActionResult =
        withContext(Dispatchers.IO) {

            try {

                val updated =
                    apiCall()

                session.updateCachedTrip(
                    updated
                )

                ActionResult.Applied(
                    updated
                )

            } catch (error: IOException) {

                queue.enqueue(
                    offlineAction
                )

                SyncWorker.enqueue(
                    context
                )

                val cached =
                    session
                        .cachedTrips()
                        .firstOrNull {
                            it.id == tripId
                        }

                if (cached != null) {

                    val local =
                        cached.copy(
                            status =
                                localStatus
                        )

                    session.updateCachedTrip(
                        local
                    )

                    ActionResult.Queued(
                        local
                    )

                } else {

                    ActionResult.Queued(
                        null
                    )
                }

            } catch (error: HttpException) {

                ActionResult.Failed(
                    code = error.code(),
                    message =
                        httpMessage(
                            error
                        ).message
                )

            } catch (error: Exception) {

                ActionResult.Failed(
                    code = null,
                    message =
                        error.message
                )
            }
        }
}

data class LocalLocationUpdate(
    val tripId: Long,
    val latitude: Double,
    val longitude: Double
)

enum class ActionResultType {
    APPLIED,
    QUEUED,
    FAILED
}

sealed interface ActionResult {

    val type: ActionResultType

    data class Applied(
        val trip: Trip
    ) : ActionResult {
        override val type =
            ActionResultType.APPLIED
    }

    data class Queued(
        val trip: Trip?
    ) : ActionResult {
        override val type =
            ActionResultType.QUEUED
    }

    data class Failed(
        val code: Int?,
        val message: String?
    ) : ActionResult {
        override val type =
            ActionResultType.FAILED
    }
}