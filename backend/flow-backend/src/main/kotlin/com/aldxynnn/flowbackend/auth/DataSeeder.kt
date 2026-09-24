package com.aldxynnn.flowbackend.auth

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Fresh installations are intentionally empty. The only automatic record allowed is
 * an administrator explicitly requested through FLOW_BOOTSTRAP_ADMIN_* environment variables.
 */
@Configuration
class DataSeeder {
    @Bean
    fun bootstrapAdmin(
        users: UserRepository,
        encoder: PasswordEncoder,
        environment: Environment
    ): CommandLineRunner = CommandLineRunner {
        val username: String = environment.getProperty("FLOW_BOOTSTRAP_ADMIN_USERNAME") ?: ""
        val password: String = environment.getProperty("FLOW_BOOTSTRAP_ADMIN_PASSWORD") ?: ""
        if (username.trim().isBlank() || password.isBlank() || users.findByUsername(username) != null) return@CommandLineRunner

        users.save(
            User(
                username = username.trim(),
                passwordHash = encoder.encode(password) ?: throw IllegalStateException("Password encoder returned null"),
                role = Role.ADMIN
            )
        )
    }
}
