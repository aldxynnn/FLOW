package com.aldxynnn.flowbackend.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val users: UserRepository
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header?.startsWith("Bearer ") == true && SecurityContextHolder.getContext().authentication == null) {
            val token = header.removePrefix("Bearer ").trim()
            try {
                val username = jwtService.username(token)
                val user = users.findByUsername(username)
                if (user != null && user.enabled) {
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
                    val auth = UsernamePasswordAuthenticationToken(user.username, null, authorities)
                    SecurityContextHolder.getContext().authentication = auth
                }
            } catch (_: Exception) {
                // Invalid/expired tokens simply remain unauthenticated.
            }
        }
        filterChain.doFilter(request, response)
    }
}
