package com.aldxynnn.flowbackend.auth

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): LoginResponse {
        val (user, token) = authService.login(
            request.username,
            request.password
        )

        return LoginResponse(
            accessToken = token,
            role = user.role,
            username = user.username
        )
    }

    @GetMapping("/me")
    fun me(principal: java.security.Principal): MeResponse {
        val auth = SecurityContextHolder
            .getContext()
            .authentication

        val role = auth?.authorities
            ?.firstOrNull()
            ?.authority
            ?.removePrefix("ROLE_")
            ?.let { Role.valueOf(it) }
            ?: Role.DRIVER

        return MeResponse(
            principal.name,
            role
        )
    }
}