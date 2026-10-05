package com.aldxynnn.flowbackend.trip

import org.springframework.data.jpa.repository.JpaRepository

interface TripRepository : JpaRepository<Trip, Long> {
    fun findAllByDriverUsernameOrderByIdDesc(driverUsername: String): List<Trip>
    fun findAllByOrderByIdDesc(): List<Trip>
}
