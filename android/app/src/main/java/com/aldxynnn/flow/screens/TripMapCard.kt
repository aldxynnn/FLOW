package com.aldxynnn.flow.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aldxynnn.flow.core.network.Trip
import com.aldxynnn.flow.core.network.TripLocation
import de.afarber.openmapview.LatLng
import de.afarber.openmapview.MapType
import de.afarber.openmapview.Marker
import de.afarber.openmapview.OpenMapView
import de.afarber.openmapview.Polyline

private val MapGreen = Color(0xFF00B14F)
private val MapInk = Color(0xFF17211B)
private val MapMuted = Color(0xFF758079)
private val MapWhite = Color.White
private val MapBorder = Color(0xFFE1E7E3)

private const val FALLBACK_LATITUDE = -6.2088
private const val FALLBACK_LONGITUDE = 106.8456

@Composable
fun TripMapCard(
    trip: Trip,
    locations: List<TripLocation>
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val latest = locations.lastOrNull()

    val currentLatitude = latest?.latitude ?: trip.latitude
    val currentLongitude = latest?.longitude ?: trip.longitude

    val centerLatitude = currentLatitude ?: FALLBACK_LATITUDE
    val centerLongitude = currentLongitude ?: FALLBACK_LONGITUDE

    var mapView by remember {
        mutableStateOf<OpenMapView?>(null)
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            mapView?.let {
                lifecycleOwner.lifecycle.removeObserver(it)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MapWhite,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MapBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "LIVE TRACKING",
                        color = MapGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )

                    Text(
                        text =
                            if (latest != null) {
                                "GPS route and current vehicle position"
                            } else {
                                "Waiting for GPS signal"
                            },
                        color = MapInk,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${locations.size} points",
                    color = MapMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { viewContext ->
                        OpenMapView(viewContext).apply {
                            lifecycleOwner.lifecycle.addObserver(this)

                            setMapType(MapType.STANDARD)

                            getUiSettings().apply {
                                isZoomControlsEnabled = false
                                isZoomGesturesEnabled = true
                                isScrollGesturesEnabled = true
                            }

                            setCenter(
                                LatLng(
                                    centerLatitude,
                                    centerLongitude
                                )
                            )

                            setZoom(15f)

                            mapView = this
                        }
                    },
                    update = { map ->
                        val routePoints =
                            locations.map {
                                LatLng(
                                    it.latitude,
                                    it.longitude
                                )
                            }

                        map.clearPolylines()
                        map.clearMarkers()

                        if (routePoints.size >= 2) {
                            map.addPolyline(
                                Polyline(
                                    points = routePoints,
                                    strokeColor = MapGreen,
                                    strokeWidth = 5f,
                                    geodesic = false
                                )
                            )
                        }

                        val markerPosition =
                            latest?.let {
                                LatLng(
                                    it.latitude,
                                    it.longitude
                                )
                            }
                                ?: trip.latitude?.let { lat ->
                                    trip.longitude?.let { lon ->
                                        LatLng(lat, lon)
                                    }
                                }

                        markerPosition?.let { position ->
                            map.addMarker(
                                Marker(
                                    position = position,
                                    title =
                                        "${trip.code} · Live GPS"
                                )
                            )

                            map.setCenter(position)
                        }
                    }
                )
            }

            Text(
                text = latest?.let {
                    "Live · %.5f, %.5f"
                        .format(
                            it.latitude,
                            it.longitude
                        )
                } ?: "GPS position will appear here once the driver starts the trip",
                color =
                    if (latest != null) {
                        MapGreen
                    } else {
                        MapMuted
                    },
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
