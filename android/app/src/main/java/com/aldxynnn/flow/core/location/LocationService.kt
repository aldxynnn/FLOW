package com.aldxynnn.flow.core.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.aldxynnn.flow.FlowApplication
import com.aldxynnn.flow.R
import com.aldxynnn.flow.core.network.LocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest as GmsLocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var callback: LocationCallback
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeTripId: Long? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopTracking()
            stopSelf()
            return START_NOT_STICKY
        }

        val tripId = intent?.getLongExtra(EXTRA_TRIP_ID, -1L) ?: -1L
        if (tripId <= 0L) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_flow_location)
            .setContentTitle("FLOW trip tracking")
            .setContentText("Live location is being shared for trip $tripId")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        startForeground(NOTIFICATION_ID, notification)
        if (activeTripId != tripId) {
            stopLocationUpdates()
            activeTripId = tripId
            startLocationUpdates(tripId)
        }
        return START_STICKY
    }

    private fun startLocationUpdates(tripId: Long) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) return

        val request = GmsLocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15_000L)
            .setMinUpdateIntervalMillis(8_000L)
            .setWaitForAccurateLocation(false)
            .build()

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                scope.launch {
                    val app = applicationContext as FlowApplication
                    app.repository.updateLocation(
                        tripId,
                        location.latitude,
                        location.longitude
                    )
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(request, callback, mainLooper)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null && activeTripId == tripId) {
                scope.launch {
                    val app = applicationContext as FlowApplication
                    app.repository.updateLocation(
                        tripId,
                        location.latitude,
                        location.longitude
                    )
                }
            }
        }
    }

    private fun stopLocationUpdates() {
        if (::callback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    private fun stopTracking() {
        stopLocationUpdates()
        activeTripId = null
    }

    override fun onDestroy() {
        stopTracking()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "FLOW trip tracking", NotificationManager.IMPORTANCE_LOW)
        )
    }

    companion object {
        const val EXTRA_TRIP_ID = "trip_id"
        const val ACTION_STOP = "com.aldxynnn.flow.location.STOP"
        private const val CHANNEL_ID = "flow_trip_tracking"
        private const val NOTIFICATION_ID = 2001
    }
}
