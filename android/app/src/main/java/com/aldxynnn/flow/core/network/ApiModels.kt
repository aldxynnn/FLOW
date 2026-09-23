package com.aldxynnn.flow.core.network

data class LoginRequest(val username: String, val password: String)

data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val role: String,
    val username: String
)

data class MeResponse(val username: String, val role: String)

data class Trip(
    val id: Long,
    val code: String,
    val origin: String,
    val destination: String,
    val vehiclePlate: String,
    val driverUsername: String,
    val status: String,
    val etaMinutes: Int,
    val startedAt: String?,
    val completedAt: String?,
    val latitude: Double?,
    val longitude: Double?,
    val lastLocationAt: String?
)

data class LocationRequest(val latitude: Double, val longitude: Double)
data class CreateTripRequest(val origin: String, val destination: String, val vehiclePlate: String, val driverUsername: String, val etaMinutes: Int)
data class AssignTripRequest(val driverUsername: String)

data class DriverSummary(val username: String, val role: String, val enabled: Boolean)
data class Vehicle(val id: Long, val plateNumber: String, val model: String, val type: String, val status: String)
data class VehicleRequest(val plateNumber: String, val model: String, val type: String, val status: String = "AVAILABLE")
data class DashboardSummary(
    val activeTrips: Long,
    val assignedTrips: Long,
    val completedTrips: Long,
    val totalTrips: Long,
    val activeDrivers: Long,
    val activeVehicles: Long,
    val availableVehicles: Long
)
data class UserSummary(val id: Long, val username: String, val role: String, val enabled: Boolean)
data class CreateUserRequest(val username: String, val password: String, val role: String, val enabled: Boolean = true)
data class SetUserEnabledRequest(val enabled: Boolean)

data class ApiError(val message: String?)
