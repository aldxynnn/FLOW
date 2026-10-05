package com.aldxynnn.flowbackend.trip

import com.aldxynnn.flowbackend.auth.UserRepository
import com.aldxynnn.flowbackend.vehicle.Vehicle
import com.aldxynnn.flowbackend.vehicle.VehicleRepository
import com.aldxynnn.flowbackend.vehicle.VehicleStatus
import com.aldxynnn.flowbackend.websocket.OpsEventBroadcaster
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class TripServiceTest {

    @Mock
    lateinit var trips: TripRepository

    @Mock
    lateinit var users: UserRepository

    @Mock
    lateinit var vehicles: VehicleRepository

    @Mock
    lateinit var broadcaster: OpsEventBroadcaster

    @Mock
    lateinit var locationHistory: TripLocationRepository

    private lateinit var service: TripService
    private lateinit var trip: Trip
    private lateinit var vehicle: Vehicle

    @BeforeEach
    fun setUp() {
        service = TripService(
            trips = trips,
            users = users,
            vehicles = vehicles,
            broadcaster = broadcaster,
            locationHistory = locationHistory
        )

        trip = Trip(
            id = 10L,
            code = "TRP-10",
            origin = "Jakarta",
            destination = "Bekasi",
            vehiclePlate = "B 1234 FLOW",
            driverUsername = "driver01",
            status = TripStatus.ASSIGNED,
            etaMinutes = 30
        )

        vehicle = Vehicle(
            1L,
            "B 1234 FLOW",
            "Isuzu",
            "Truck",
            VehicleStatus.AVAILABLE
        )

        `when`(trips.findById(10L))
            .thenReturn(Optional.of(trip))
    }

    @Test
    fun `driver can start assigned trip`() {
        `when`(vehicles.findByPlateNumber("B 1234 FLOW"))
            .thenReturn(vehicle)

        `when`(trips.save(trip))
            .thenAnswer { it.arguments[0] }

        val result = service.start(
            10L,
            "driver01"
        )

        assertEquals(
            TripStatus.IN_PROGRESS,
            result.status
        )

        verify(vehicles).save(vehicle)

        verify(broadcaster).broadcast(
            "trip.started",
            result
        )
    }

    @Test
    fun `driver can complete active trip`() {
        trip.status = TripStatus.IN_PROGRESS
        vehicle.status = VehicleStatus.ON_TRIP

        `when`(vehicles.findByPlateNumber("B 1234 FLOW"))
            .thenReturn(vehicle)

        `when`(trips.save(trip))
            .thenAnswer { it.arguments[0] }

        val result = service.complete(
            10L,
            "driver01"
        )

        assertEquals(
            TripStatus.COMPLETED,
            result.status
        )

        assertEquals(
            0,
            result.etaMinutes
        )

        assertEquals(
            VehicleStatus.AVAILABLE,
            vehicle.status
        )

        verify(vehicles).save(vehicle)

        verify(broadcaster).broadcast(
            "trip.completed",
            result
        )
    }

    @Test
    fun `driver location update changes last known location`() {
        trip.status = TripStatus.IN_PROGRESS

        `when`(trips.save(trip))
            .thenAnswer { it.arguments[0] }

        val result = service.location(
            10L,
            "driver01",
            LocationRequest(
                latitude = -6.2,
                longitude = 106.8
            )
        )

        assertEquals(
            -6.2,
            result.latitude
        )

        assertEquals(
            106.8,
            result.longitude
        )

        verify(locationHistory).save(any(TripLocation::class.java))

        verify(broadcaster).broadcast(
            "trip.location",
            result
        )
    }

    @Test
    fun `trip location history is returned in time order`() {
        `when`(locationHistory.findAllByTripIdOrderByRecordedAtAsc(10L))
            .thenReturn(
                listOf(
                    TripLocation(
                        id = 1L,
                        tripId = 10L,
                        latitude = -6.2,
                        longitude = 106.8,
                        recordedAt = java.time.Instant.parse("2026-09-24T06:00:00Z")
                    ),
                    TripLocation(
                        id = 2L,
                        tripId = 10L,
                        latitude = -6.21,
                        longitude = 106.81,
                        recordedAt = java.time.Instant.parse("2026-09-24T06:00:15Z")
                    )
                )
            )

        val result = service.locations(10L, "driver01", com.aldxynnn.flowbackend.auth.Role.DRIVER)

        assertEquals(2, result.size)
        assertEquals(-6.2, result[0].latitude)
        assertEquals(106.81, result[1].longitude)
    }
}