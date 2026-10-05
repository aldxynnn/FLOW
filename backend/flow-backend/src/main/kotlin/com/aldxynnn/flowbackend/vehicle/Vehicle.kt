package com.aldxynnn.flowbackend.vehicle

import jakarta.persistence.*

@Entity
@Table(name = "vehicles")
class Vehicle(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true, length = 30)
    var plateNumber: String,
    @Column(nullable = false, length = 80)
    var model: String,
    @Column(nullable = false, length = 20)
    var type: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: VehicleStatus = VehicleStatus.AVAILABLE
)
