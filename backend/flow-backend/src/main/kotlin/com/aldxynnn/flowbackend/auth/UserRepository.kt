package com.aldxynnn.flowbackend.auth

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun countByRoleAndEnabled(role: Role, enabled: Boolean): Long
    fun findByUsername(username: String): User?
}
