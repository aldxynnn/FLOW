package com.aldxynnn.flowbackend.auth

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
class UserController(
    private val users: UserRepository,
    private val encoder: PasswordEncoder
) {
    @GetMapping
    fun list(): List<UserResponse> = users.findAll().sortedBy { it.username }.map(UserResponse::from)

    @PostMapping
    fun create(@Valid @RequestBody request: CreateUserRequest): UserResponse {
        val username: String = request.username.trim()
        val password: String = request.password
        require(users.findByUsername(username) == null) { "Username already exists" }
        require(request.role != Role.ADMIN || users.findAll().none { it.role == Role.ADMIN && it.enabled }) {
            "An enabled admin account already exists"
        }
        return UserResponse.from(users.save(User(
            username = username,
            passwordHash = encoder.encode(password)
                ?: throw IllegalStateException("Password encoder returned null"),
            role = request.role,
            enabled = request.enabled
        )))
    }

    @PatchMapping("/{id}/enabled")
    fun setEnabled(@PathVariable id: Long, @RequestBody request: SetUserEnabledRequest): UserResponse {
        val user = users.findById(id).orElseThrow { NoSuchElementException("User not found") }
        user.enabled = request.enabled
        return UserResponse.from(users.save(user))
    }
}
