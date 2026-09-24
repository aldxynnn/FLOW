package com.aldxynnn.flowbackend.dashboard

import com.aldxynnn.flowbackend.trip.TripRepository
import com.aldxynnn.flowbackend.trip.TripStatus
import com.aldxynnn.flowbackend.auth.Role
import com.aldxynnn.flowbackend.auth.UserRepository
import com.aldxynnn.flowbackend.vehicle.VehicleRepository
import com.aldxynnn.flowbackend.vehicle.VehicleStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.access.prepost.PreAuthorize

data class DashboardSummary(
    val activeTrips: Long,
    val assignedTrips: Long,
    val completedTrips: Long,
    val totalTrips: Long,
    val activeDrivers: Long,
    val activeVehicles: Long,
    val availableVehicles: Long
)

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    private val trips: TripRepository,
    private val users: UserRepository,
    private val vehicles: VehicleRepository
) {
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DISPATCHER')")
    @GetMapping("/summary")
    fun summary(): DashboardSummary {
        val all = trips.findAll()
        return DashboardSummary(
            activeTrips = all.count { it.status == TripStatus.IN_PROGRESS }.toLong(),
            assignedTrips = all.count { it.status == TripStatus.ASSIGNED }.toLong(),
            completedTrips = all.count { it.status == TripStatus.COMPLETED }.toLong(),
            totalTrips = all.size.toLong(),
            activeDrivers = users.countByRoleAndEnabled(Role.DRIVER, true),
            activeVehicles = vehicles.countByStatus(VehicleStatus.ON_TRIP),
            availableVehicles = vehicles.countByStatus(VehicleStatus.AVAILABLE)
        )
    }
}
