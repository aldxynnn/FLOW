package com.aldxynnn.flow.screens

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aldxynnn.flow.core.network.Trip
import com.aldxynnn.flow.core.network.TripLocation
import java.util.Locale

private val MapGreen = Color(0xFF0B7A55)
private val MapInk = Color(0xFF112F25)
private val MapMuted = Color(0xFF66756E)
private val MapWhite = Color.White
private val MapBorder = Color(0xFFD9E3DE)

private const val FALLBACK_LATITUDE = -6.2088
private const val FALLBACK_LONGITUDE = 106.8456

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TripMapCard(
    trip: Trip,
    locations: List<TripLocation>
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val orderedLocations = locations
        .sortedBy { it.recordedAt }

    val latest = orderedLocations.lastOrNull()
    val currentLatitude = latest?.latitude ?: trip.latitude
    val currentLongitude = latest?.longitude ?: trip.longitude
    val centerLatitude = currentLatitude ?: FALLBACK_LATITUDE
    val centerLongitude = currentLongitude ?: FALLBACK_LONGITUDE

    var mapWebView by remember { mutableStateOf<WebView?>(null) }
    var pageReady by remember { mutableStateOf(false) }

    fun mapJson(): String {
        val points = orderedLocations.joinToString(",") { point ->
            "{\"latitude\":${point.latitude},\"longitude\":${point.longitude},\"recordedAt\":\"${point.recordedAt.orEmpty().replace("\"", "\\\"")}\"}"
        }
        val fallback = if (orderedLocations.isEmpty() && currentLatitude != null && currentLongitude != null) {
            "{\"latitude\":$centerLatitude,\"longitude\":$centerLongitude,\"recordedAt\":\"${trip.lastLocationAt.orEmpty()}\"}"
        } else {
            ""
        }
        val payload = if (points.isNotEmpty()) points else fallback
        return "{\"points\":[$payload],\"center\":{\"latitude\":$centerLatitude,\"longitude\":$centerLongitude},\"code\":\"${trip.code.replace("\"", "\\\"")}\"}"
    }

    fun pushMapData(webView: WebView) {
        if (!pageReady) return
        val json = mapJson()
        webView.evaluateJavascript("window.updateFlowMap && window.updateFlowMap($json);", null)
    }

    DisposableEffect(lifecycleOwner, locations, trip.latitude, trip.longitude, trip.lastLocationAt) {
        val observer = LifecycleEventObserver { _, event ->
            val webView = mapWebView ?: return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_RESUME -> webView.onResume()
                Lifecycle.Event.ON_PAUSE -> webView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        mapWebView?.let(::pushMapData)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapWebView?.apply {
                stopLoading()
                loadUrl("about:blank")
                onPause()
                destroy()
            }
            mapWebView = null
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LIVE TRACKING",
                        color = MapGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = if (latest != null) {
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
                        WebView(viewContext).apply {
                            setBackgroundColor(AndroidColor.rgb(245, 247, 246))
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    pageReady = true
                                    pushMapData(this@apply)
                                }
                            }
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.cacheMode = WebSettings.LOAD_DEFAULT
                            settings.allowFileAccess = true
                            settings.allowContentAccess = true
                            settings.allowFileAccessFromFileURLs = true
                            settings.allowUniversalAccessFromFileURLs = true
                            loadUrl("file:///android_asset/flow_map.html")
                            mapWebView = this
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    update = { webView ->
                        mapWebView = webView
                        pushMapData(webView)
                    }
                )
            }

            Text(
                text = latest?.let {
                    String.format(Locale.US, "Live · %.5f, %.5f", it.latitude, it.longitude)
                } ?: "GPS position will appear here once the driver starts the trip",
                color = if (latest != null) MapGreen else MapMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
