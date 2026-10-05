package com.aldxynnn.flowbackend.trip

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import java.time.Instant

data class CreateTripRequest(
    @field:NotBlank val origin: String,
    @field:NotBlank val destination: String,
    @field:NotBlank val vehiclePlate: String,
    @field:NotBlank val driverUsername: String,
    @field:PositiveOrZero val etaMinutes: Int = 0
)

data class AssignTripRequest(
    @field:NotBlank val driverUsername: String
)

data class LocationRequest(
    @field:DecimalMin("-90.0")
    @field:DecimalMax("90.0")
    val latitude: Double,
    @field:DecimalMin("-180.0")
    @field:DecimalMax("180.0")
    val longitude: Double
)

data class TripLocationResponse(
    val latitude: Double,
    val longitude: Double,
    val recordedAt: Instant
) {
    companion object {
        fun from(location: TripLocation) = TripLocationResponse(
            latitude = location.latitude,
            longitude = location.longitude,
            recordedAt = location.recordedAt
        )
    }
}

data class TripResponse(
    val id: Long,
    val code: String,
    val origin: String,
    val destination: String,
    val vehiclePlate: String,
    val driverUsername: String,
    val status: TripStatus,
    val etaMinutes: Int,
    val startedAt: Instant?,
    val completedAt: Instant?,
    val latitude: Double?,
    val longitude: Double?,
    val lastLocationAt: Instant?
) {
    companion object {
        fun from(trip: Trip) = TripResponse(
            id = requireNotNull(trip.id),
            code = trip.code,
            origin = trip.origin,
            destination = trip.destination,
            vehiclePlate = trip.vehiclePlate,
            driverUsername = trip.driverUsername,
            status = trip.status,
            etaMinutes = trip.etaMinutes,
            startedAt = trip.startedAt,
            completedAt = trip.completedAt,
            latitude = trip.latitude,
            longitude = trip.longitude,
            lastLocationAt = trip.lastLocationAt
        )
    }
}
