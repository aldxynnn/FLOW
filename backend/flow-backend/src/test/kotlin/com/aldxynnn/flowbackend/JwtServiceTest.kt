package com.aldxynnn.flowbackend

import com.aldxynnn.flowbackend.auth.JwtService
import com.aldxynnn.flowbackend.auth.Role
import com.aldxynnn.flowbackend.auth.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

class JwtServiceTest {
    @Test
    fun `generated token contains username`() {
        val secret = "0123456789012345678901234567890123456789012345678901234567890123"
        val service = JwtService(secret, 60_000)
        val user = User(username = "driver01", passwordHash = "hash", role = Role.DRIVER)
        val token = service.generate(user)
        assertEquals("driver01", service.username(token))
    }
}
