package com.aldxynnn.flowbackend.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

 data class LoginRequest(
    @field:NotBlank val username: String,
    @field:NotBlank @field:Size(min = 8) val password: String
 )

data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val role: Role,
    val username: String
)

data class MeResponse(
    val username: String,
    val role: Role
)
