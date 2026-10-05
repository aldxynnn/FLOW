package com.aldxynnn.flowbackend.vehicle

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vehicles")
class VehicleController(private val repository: VehicleRepository) {
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DISPATCHER')")
    @GetMapping
    fun list(): List<VehicleResponse> = repository.findAll().map(VehicleResponse::from)

    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    @PostMapping
    fun create(@Valid @RequestBody request: VehicleRequest): VehicleResponse {
        require(repository.findByPlateNumber(request.plateNumber.trim().uppercase()) == null) { "Vehicle plate already exists" }
        val v = repository.save(Vehicle(plateNumber = request.plateNumber.trim().uppercase(), model = request.model.trim(), type = request.type.trim(), status = request.status))
        return VehicleResponse.from(v)
    }

    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    @PatchMapping("/{id}/status")
    fun setStatus(@PathVariable id: Long, @RequestParam status: VehicleStatus): VehicleResponse {
        val v = repository.findById(id).orElseThrow { NoSuchElementException("Vehicle not found") }
        v.status = status
        return VehicleResponse.from(repository.save(v))
    }
}
