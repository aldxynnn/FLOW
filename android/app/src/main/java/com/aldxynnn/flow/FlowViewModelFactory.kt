package com.aldxynnn.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aldxynnn.flow.core.data.TripRepository
import com.aldxynnn.flow.core.session.SessionStore

class FlowViewModelFactory(
    private val repository: TripRepository,
    private val sessionStore: SessionStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FlowViewModel::class.java))
        return FlowViewModel(repository, sessionStore) as T
    }
}
