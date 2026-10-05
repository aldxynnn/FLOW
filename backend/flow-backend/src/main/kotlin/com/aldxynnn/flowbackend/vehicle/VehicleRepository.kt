package com.aldxynnn.flowbackend.vehicle

import org.springframework.data.jpa.repository.JpaRepository

interface VehicleRepository : JpaRepository<Vehicle, Long> {
    fun countByStatus(status: VehicleStatus): Long
    fun findByPlateNumber(plateNumber: String): Vehicle?
}
