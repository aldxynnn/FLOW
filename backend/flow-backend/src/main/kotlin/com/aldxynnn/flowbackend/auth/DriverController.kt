package com.aldxynnn.flowbackend.auth

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.access.prepost.PreAuthorize

data class DriverSummary(val username: String, val role: Role, val enabled: Boolean)

@RestController
@RequestMapping("/api/drivers")
class DriverController(private val users: UserRepository) {
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DISPATCHER')")
    @GetMapping
    fun list(): List<DriverSummary> = users.findAll()
        .filter { it.role == Role.DRIVER }
        .sortedBy { it.username }
        .map { DriverSummary(it.username, it.role, it.enabled) }
}
