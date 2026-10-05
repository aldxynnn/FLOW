package com.aldxynnn.flow.core.data

data class QueuedAction(
    val type: String,
    val tripId: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val attempts: Int = 0
)
