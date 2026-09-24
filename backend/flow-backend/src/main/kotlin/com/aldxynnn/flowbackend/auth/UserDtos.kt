package com.aldxynnn.flowbackend.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserResponse(
    val id: Long,
    val username: String,
    val role: Role,
    val enabled: Boolean
) {
    companion object {
        fun from(user: User) = UserResponse(requireNotNull(user.id), user.username, user.role, user.enabled)
    }
}

data class CreateUserRequest(
    @field:NotBlank @field:Size(max = 80) val username: String,
    @field:NotBlank @field:Size(min = 8, max = 120) val password: String,
    val role: Role,
    val enabled: Boolean = true
)

data class SetUserEnabledRequest(val enabled: Boolean)
