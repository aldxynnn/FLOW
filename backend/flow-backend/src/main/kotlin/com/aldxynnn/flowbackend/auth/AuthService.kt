package com.aldxynnn.flowbackend.auth

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val users: UserRepository,
    private val encoder: PasswordEncoder,
    private val jwt: JwtService
) {
    fun login(username: String, password: String): Pair<User, String> {
        val normalized = username.trim()
        val user = users.findByUsername(normalized)
            ?: throw com.aldxynnn.flowbackend.common.UnauthorizedException("Invalid username or password")

        if (!user.enabled || !encoder.matches(password, user.passwordHash)) {
            throw com.aldxynnn.flowbackend.common.UnauthorizedException("Invalid username or password")
        }

        return user to jwt.generate(user)
    }
}
