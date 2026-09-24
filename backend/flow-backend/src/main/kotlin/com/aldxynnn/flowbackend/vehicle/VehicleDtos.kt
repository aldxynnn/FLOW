package com.aldxynnn.flowbackend.vehicle

import jakarta.validation.constraints.NotBlank

data class VehicleRequest(
    @field:NotBlank val plateNumber: String,
    @field:NotBlank val model: String,
    @field:NotBlank val type: String,
    val status: VehicleStatus = VehicleStatus.AVAILABLE
)

data class VehicleResponse(val id: Long, val plateNumber: String, val model: String, val type: String, val status: VehicleStatus) {
    companion object { fun from(v: Vehicle) = VehicleResponse(requireNotNull(v.id), v.plateNumber, v.model, v.type, v.status) }
}
