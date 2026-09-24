package com.aldxynnn.flowbackend.websocket

import com.aldxynnn.flowbackend.auth.JwtService
import com.aldxynnn.flowbackend.auth.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import java.net.URI

@Configuration
@EnableWebSocket
class OpsWebSocketConfig(
    private val broadcaster: OpsEventBroadcaster,
    private val jwtService: JwtService,
    private val users: UserRepository,
    @Value("\${flow.websocket.allowed-origins}") private val allowedOrigins: String
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        val handler = object : TextWebSocketHandler() {
            override fun afterConnectionEstablished(session: WebSocketSession) { broadcaster.register(session) }
            override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) { broadcaster.remove(session) }
            override fun handleTextMessage(session: WebSocketSession, message: TextMessage) { /* read-only channel */ }
        }

        registry.addHandler(handler, "/ws/ops")
            .addInterceptors(AuthHandshakeInterceptor(jwtService, users))
            .setAllowedOriginPatterns(*allowedOrigins.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toTypedArray())
    }
}

private class AuthHandshakeInterceptor(
    private val jwtService: JwtService,
    private val users: UserRepository
) : HandshakeInterceptor {
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val token = request.headers.getFirst(HttpHeaders.AUTHORIZATION)?.removePrefix("Bearer ")?.trim()
            ?: parseQueryToken(request.uri)
            ?: return false
        return runCatching {
            val username = jwtService.username(token)
            val user = users.findByUsername(username)
            if (user == null || !user.enabled) false else {
                attributes["username"] = username
                true
            }
        }.getOrDefault(false)
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) = Unit

    private fun parseQueryToken(uri: URI): String? =
        uri.rawQuery?.split('&')?.mapNotNull { part ->
            val pair = part.split('=', limit = 2)
            if (pair.firstOrNull() == "access_token") pair.getOrNull(1) else null
        }?.firstOrNull()
}
