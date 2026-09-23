package com.aldxynnn.flowbackend.trip

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "trips")
class Trip(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 40)
    var code: String,

    @Column(nullable = false, length = 120)
    var origin: String,

    @Column(nullable = false, length = 120)
    var destination: String,

    @Column(nullable = false, length = 30)
    var vehiclePlate: String,

    @Column(nullable = false, length = 80)
    var driverUsername: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: TripStatus = TripStatus.ASSIGNED,

    @Column(nullable = false)
    var etaMinutes: Int = 0,

    var startedAt: Instant? = null,
    var completedAt: Instant? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var lastLocationAt: Instant? = null
)
