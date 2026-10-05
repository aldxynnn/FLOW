package com.aldxynnn.flowbackend.health

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/health")
class HealthController {
    @GetMapping
    fun health() = mapOf(
        "status" to "UP",
        "service" to "flow-backend"
    )
}
