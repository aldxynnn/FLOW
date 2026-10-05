package com.aldxynnn.flow.core.work

object SyncPolicy {
    enum class Decision { RETRY, CLEAR_SESSION_AND_RETAIN, DROP }

    fun forHttpStatus(code: Int): Decision = when {
        code == 401 -> Decision.CLEAR_SESSION_AND_RETAIN
        code == 409 -> Decision.DROP
        code == 429 || code in 500..599 -> Decision.RETRY
        else -> Decision.DROP
    }
}
