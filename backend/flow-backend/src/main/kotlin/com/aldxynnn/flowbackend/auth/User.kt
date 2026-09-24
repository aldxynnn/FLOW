package com.aldxynnn.flowbackend.auth

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 80)
    var username: String,

    @Column(name = "password_hash", nullable = false, length = 120)
    var passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var role: Role,

    @Column(nullable = false)
    var enabled: Boolean = true
)
