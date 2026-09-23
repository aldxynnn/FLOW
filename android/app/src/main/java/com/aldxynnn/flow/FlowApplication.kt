package com.aldxynnn.flow

import android.app.Application
import com.aldxynnn.flow.core.data.ActionQueue
import com.aldxynnn.flow.core.data.TripRepository
import com.aldxynnn.flow.core.network.ApiProvider
import com.aldxynnn.flow.core.session.SessionStore

class FlowApplication : Application() {
    lateinit var sessionStore: SessionStore
        private set
    lateinit var actionQueue: ActionQueue
        private set
    lateinit var apiProvider: ApiProvider
        private set
    lateinit var repository: TripRepository
        private set

    override fun onCreate() {
        super.onCreate()
        sessionStore = SessionStore(this)
        actionQueue = ActionQueue(this)
        apiProvider = ApiProvider(sessionStore)
        repository = TripRepository(apiProvider.service, sessionStore, actionQueue, this)
    }
}
