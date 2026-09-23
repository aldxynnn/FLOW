package com.aldxynnn.flow.core.work

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncPolicyTest {
    @Test fun unauthorizedRetainsAction() {
        assertEquals(SyncPolicy.Decision.CLEAR_SESSION_AND_RETAIN, SyncPolicy.forHttpStatus(401))
    }

    @Test fun conflictDropsAction() {
        assertEquals(SyncPolicy.Decision.DROP, SyncPolicy.forHttpStatus(409))
    }

    @Test fun serverErrorsRetry() {
        assertEquals(SyncPolicy.Decision.RETRY, SyncPolicy.forHttpStatus(503))
        assertEquals(SyncPolicy.Decision.RETRY, SyncPolicy.forHttpStatus(429))
    }

    @Test fun validationErrorsDropAction() {
        assertEquals(SyncPolicy.Decision.DROP, SyncPolicy.forHttpStatus(400))
    }
}
