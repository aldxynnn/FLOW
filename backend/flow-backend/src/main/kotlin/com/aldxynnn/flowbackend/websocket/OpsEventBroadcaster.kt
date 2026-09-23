package com.aldxynnn.flowbackend.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.CopyOnWriteArraySet

data class OpsEvent(val type: String, val data: Any)

@Component
class OpsEventBroadcaster(private val mapper: ObjectMapper) {
    private val sessions = CopyOnWriteArraySet<WebSocketSession>()

    fun register(session: WebSocketSession) { sessions += session }
    fun remove(session: WebSocketSession) { sessions -= session }

    fun broadcast(type: String, data: Any) {
        val message = TextMessage(mapper.writeValueAsString(OpsEvent(type, data)))
        sessions.toList().forEach { session ->
            runCatching { if (session.isOpen) session.sendMessage(message) }
        }
    }
}
