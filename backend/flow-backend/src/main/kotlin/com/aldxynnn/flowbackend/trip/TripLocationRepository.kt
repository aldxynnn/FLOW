package com.aldxynnn.flowbackend.trip

import org.springframework.data.jpa.repository.JpaRepository

interface TripLocationRepository : JpaRepository<TripLocation, Long> {
    fun findAllByTripIdOrderByRecordedAtAsc(tripId: Long): List<TripLocation>
}
