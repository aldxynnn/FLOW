package com.aldxynnn.flowbackend.trip

import com.aldxynnn.flowbackend.auth.Role
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/trips")
class TripController(private val service: TripService) {

    @GetMapping
    fun list(authentication: Authentication): List<TripResponse> {
        val authority = requireNotNull(
            authentication.authorities.firstOrNull()?.authority
        ) {
            "User authentication authority is missing"
        }

        val role = authority
            .removePrefix("ROLE_")
            .let { com.aldxynnn.flowbackend.auth.Role.valueOf(it) }

        return service.listFor(authentication.name, role)
    }

    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateTripRequest
    ): TripResponse = service.create(request)

    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DISPATCHER')")
    @PostMapping("/{id}/assign")
    fun assign(
        @PathVariable id: Long,
        @Valid @RequestBody request: AssignTripRequest
    ): TripResponse = service.assign(id, request)

    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{id}/start")
    fun start(
        @PathVariable id: Long,
        authentication: Authentication
    ): TripResponse = service.start(id, authentication.name)

    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{id}/complete")
    fun complete(
        @PathVariable id: Long,
        authentication: Authentication
    ): TripResponse = service.complete(id, authentication.name)

    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{id}/location")
    fun location(
        @PathVariable id: Long,
        authentication: Authentication,
        @Valid @RequestBody request: LocationRequest
    ): TripResponse =
        service.location(id, authentication.name, request)

    @GetMapping("/{id}/locations")
    fun locations(
        @PathVariable id: Long,
        authentication: Authentication
    ): List<TripLocationResponse> {
        val authority = requireNotNull(authentication.authorities.firstOrNull()?.authority) {
            "User authentication authority is missing"
        }
        val role = Role.valueOf(authority.removePrefix("ROLE_"))
        return service.locations(id, authentication.name, role)
    }
}