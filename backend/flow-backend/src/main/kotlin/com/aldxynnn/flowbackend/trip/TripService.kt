package com.aldxynnn.flowbackend.trip

import com.aldxynnn.flowbackend.auth.Role
import com.aldxynnn.flowbackend.auth.UserRepository
import com.aldxynnn.flowbackend.websocket.OpsEventBroadcaster
import com.aldxynnn.flowbackend.vehicle.VehicleRepository
import com.aldxynnn.flowbackend.vehicle.VehicleStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class TripService(
    private val trips: TripRepository,
    private val users: UserRepository,
    private val vehicles: VehicleRepository,
    private val broadcaster: OpsEventBroadcaster
) {
    fun listFor(username: String, role: Role): List<TripResponse> {
        val source = if (role == Role.DRIVER) {
            trips.findAllByDriverUsernameOrderByIdDesc(username)
        } else {
            trips.findAllByOrderByIdDesc()
        }
        return source.map(TripResponse::from)
    }

    @Transactional
    fun create(request: CreateTripRequest): TripResponse {
        val driver = users.findByUsername(request.driverUsername.trim())
        require(driver?.role == Role.DRIVER && driver.enabled) { "Enabled driver not found" }
        val vehicle = vehicles.findByPlateNumber(request.vehiclePlate.trim().uppercase()) ?: throw NoSuchElementException("Vehicle not found")
        require(vehicle.status == VehicleStatus.AVAILABLE) { "Vehicle is not available" }
        val code = "TRP-${System.currentTimeMillis()}"
        val trip = trips.save(
            Trip(
                code = code,
                origin = request.origin.trim(),
                destination = request.destination.trim(),
                vehiclePlate = request.vehiclePlate.trim().uppercase(),
                driverUsername = request.driverUsername.trim(),
                etaMinutes = request.etaMinutes
            )
        )
        vehicle.status = VehicleStatus.ON_TRIP
        vehicles.save(vehicle)
        return TripResponse.from(trip).also { broadcaster.broadcast("trip.created", it) }
    }

    @Transactional
    fun assign(id: Long, request: AssignTripRequest): TripResponse {
        val driver = users.findByUsername(request.driverUsername.trim())
        require(driver?.role == Role.DRIVER && driver.enabled) { "Enabled driver not found" }
        val trip = get(id)
        require(trip.status != TripStatus.COMPLETED) { "Completed trip cannot be reassigned" }
        trip.driverUsername = request.driverUsername.trim()
        trip.status = TripStatus.ASSIGNED
        return TripResponse.from(trips.save(trip)).also { broadcaster.broadcast("trip.assigned", it) }
    }

    @Transactional
    fun start(id: Long, username: String): TripResponse {
        val trip = get(id)
        require(trip.driverUsername == username) { "Trip is not assigned to this driver" }
        require(trip.status == TripStatus.ASSIGNED) { "Trip cannot be started from status ${trip.status}" }
        trip.status = TripStatus.IN_PROGRESS
        vehicles.findByPlateNumber(trip.vehiclePlate)?.let { it.status = VehicleStatus.ON_TRIP; vehicles.save(it) }
        trip.startedAt = Instant.now()
        return TripResponse.from(trips.save(trip)).also { broadcaster.broadcast("trip.started", it) }
    }

    @Transactional
    fun complete(id: Long, username: String): TripResponse {
        val trip = get(id)
        require(trip.driverUsername == username) { "Trip is not assigned to this driver" }
        require(trip.status == TripStatus.IN_PROGRESS) { "Trip must be in progress before completion" }
        trip.status = TripStatus.COMPLETED
        vehicles.findByPlateNumber(trip.vehiclePlate)?.let { it.status = VehicleStatus.AVAILABLE; vehicles.save(it) }
        trip.completedAt = Instant.now()
        trip.etaMinutes = 0
        return TripResponse.from(trips.save(trip)).also { broadcaster.broadcast("trip.completed", it) }
    }

    @Transactional
    fun location(id: Long, username: String, request: LocationRequest): TripResponse {
        val trip = get(id)
        require(trip.driverUsername == username) { "Trip is not assigned to this driver" }
        require(trip.status == TripStatus.IN_PROGRESS) { "Location can only be updated for an active trip" }
        trip.latitude = request.latitude
        trip.longitude = request.longitude
        trip.lastLocationAt = Instant.now()
        return TripResponse.from(trips.save(trip)).also { broadcaster.broadcast("trip.location", it) }
    }

    private fun get(id: Long): Trip = trips.findById(id).orElseThrow { NoSuchElementException("Trip not found") }
}
