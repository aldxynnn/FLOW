package com.aldxynnn.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldxynnn.flow.core.data.ActionResult
import com.aldxynnn.flow.core.data.TripRepository
import com.aldxynnn.flow.core.network.CreateTripRequest
import com.aldxynnn.flow.core.network.CreateUserRequest
import com.aldxynnn.flow.core.network.DashboardSummary
import com.aldxynnn.flow.core.network.DriverSummary
import com.aldxynnn.flow.core.network.Trip
import com.aldxynnn.flow.core.network.UserSummary
import com.aldxynnn.flow.core.network.Vehicle
import com.aldxynnn.flow.core.network.VehicleRequest
import com.aldxynnn.flow.core.session.Session
import com.aldxynnn.flow.core.session.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FlowViewModel(
    private val repository: TripRepository,
    private val sessionStore: SessionStore
) : ViewModel() {
    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()
    private val _summary = MutableStateFlow<DashboardSummary?>(null)
    val summary: StateFlow<DashboardSummary?> = _summary.asStateFlow()
    private val _drivers = MutableStateFlow<List<DriverSummary>>(emptyList())
    val drivers: StateFlow<List<DriverSummary>> = _drivers.asStateFlow()
    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()
    private val _users = MutableStateFlow<List<UserSummary>>(emptyList())
    val users: StateFlow<List<UserSummary>> = _users.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        sessionStore.session.onEach { current ->
            val changed = _session.value?.token != current?.token
            _session.value = current
            if (changed && current != null) refreshAll()
        }.launchIn(viewModelScope)
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) { _message.value = "Username dan password wajib diisi."; return }
        viewModelScope.launch {
            _loading.value = true
            runCatching { repository.login(username, password) }
                .onSuccess { response -> sessionStore.saveLogin(response); _session.value = sessionStore.currentSession(); repository.enqueuePendingSync(); refreshAll() }
                .onFailure { _message.value = "Login gagal. Periksa kredensial dan koneksi server." }
            _loading.value = false
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _loading.value = true
            repository.trips().onSuccess { _trips.value = it }.onFailure { _message.value = "Trip tidak dapat dimuat. Data tersimpan lokal dipakai bila tersedia." }
            val role = _session.value?.role.orEmpty()
            if (role != "DRIVER") {
                repository.dashboardSummary().onSuccess { _summary.value = it }
                repository.drivers().onSuccess { _drivers.value = it }
                repository.vehicles().onSuccess { _vehicles.value = it }
                if (role == "ADMIN") repository.users().onSuccess { _users.value = it }
            }
            _loading.value = false
        }
    }

    fun refreshTrips() = refreshAll()

    fun startTrip(trip: Trip) = viewModelScope.launch {
        handleActionResult(repository.startTrip(trip), "Trip dimulai. GPS aktif.", "Offline: start disimpan dan akan disinkronkan otomatis.")
    }

    fun completeTrip(trip: Trip) = viewModelScope.launch {
        handleActionResult(repository.completeTrip(trip), "Trip selesai.", "Offline: completion disimpan dan akan disinkronkan otomatis.")
    }

    fun createTrip(request: CreateTripRequest, onDone: (() -> Unit)? = null) = viewModelScope.launch {
        _loading.value = true
        repository.createTrip(request).onSuccess { _message.value = "Trip ${it.code} berhasil dibuat."; onDone?.invoke(); refreshAll() }
            .onFailure { _message.value = it.message ?: "Trip gagal dibuat." }
        _loading.value = false
    }

    fun assignTrip(trip: Trip, driverUsername: String) = viewModelScope.launch {
        _loading.value = true
        repository.assignTrip(trip.id, driverUsername).onSuccess { updated -> replaceTrip(updated); _message.value = "${trip.code} ditugaskan ke $driverUsername." }
            .onFailure { _message.value = it.message ?: "Assignment gagal." }
        _loading.value = false
    }

    fun createVehicle(request: VehicleRequest, onDone: (() -> Unit)? = null) = viewModelScope.launch {
        _loading.value = true
        repository.createVehicle(request).onSuccess { _vehicles.value = _vehicles.value + it; _message.value = "Vehicle ${it.plateNumber} added."; onDone?.invoke() }
            .onFailure { _message.value = it.message ?: "Vehicle gagal dibuat." }
        _loading.value = false
    }

    fun createUser(request: CreateUserRequest, onDone: (() -> Unit)? = null) = viewModelScope.launch {
        _loading.value = true
        repository.createUser(request).onSuccess { _users.value = _users.value + it; _message.value = "User ${it.username} dibuat."; onDone?.invoke() }
            .onFailure { _message.value = it.message ?: "User gagal dibuat." }
        _loading.value = false
    }

    fun setUserEnabled(user: UserSummary) = viewModelScope.launch {
        repository.setUserEnabled(user.id, !user.enabled).onSuccess { updated -> _users.value = _users.value.map { if (it.id == updated.id) updated else it } }
            .onFailure { _message.value = it.message ?: "Status user gagal diubah." }
    }

    fun dismissMessage() { _message.value = null }
    fun logout() = viewModelScope.launch { sessionStore.clear(); _session.value = null; _trips.value = emptyList(); _summary.value = null }

    private fun handleActionResult(result: ActionResult, success: String, queued: String) {
        when (result) {
            is ActionResult.Applied -> { replaceTrip(result.trip); _message.value = success }
            is ActionResult.Queued -> { result.trip?.let(::replaceTrip); _message.value = queued }
            is ActionResult.Failed -> _message.value = "Aksi ditolak server${result.code?.let { " ($it)" } ?: ""}."
        }
    }
    private fun replaceTrip(updated: Trip) { _trips.value = _trips.value.map { if (it.id == updated.id) updated else it } }
}
