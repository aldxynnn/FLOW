package com.aldxynnn.flowbackend.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.CopyOnWriteArraySet

data class OpsEvent(
    val type: String,
    val data: Any
)

@Component
class OpsEventBroadcaster(
    private val mapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val sessions = CopyOnWriteArraySet<WebSocketSession>()

    fun register(session: WebSocketSession) {
        sessions += session
    }

    fun remove(session: WebSocketSession) {
        sessions -= session
    }

    fun broadcast(type: String, data: Any) {
        val message = runCatching {
            TextMessage(
                mapper.writeValueAsString(
                    OpsEvent(type, data)
                )
            )
        }.getOrElse { error ->
            log.error(
                "Failed to serialize realtime event: {}",
                type,
                error
            )
            return
        }

        sessions.toList().forEach { session ->
            runCatching {
                if (session.isOpen) {
                    session.sendMessage(message)
                }
            }.onFailure { error ->
                log.debug(
                    "Failed to send realtime event to websocket session",
                    error
                )
            }
        }
    }
}