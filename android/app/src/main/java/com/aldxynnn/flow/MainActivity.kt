package com.aldxynnn.flow

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aldxynnn.flow.ui.FlowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestFlowPermissions()

        val app = application as FlowApplication

        setContent {
            FlowTheme {
                val vm: FlowViewModel = viewModel(
                    factory = FlowViewModelFactory(
                        app.repository,
                        app.sessionStore
                    )
                )

                FlowApp(vm)
            }
        }
    }

    private fun requestFlowPermissions() {
        val permissions = buildList {
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)

            if (Build.VERSION.SDK_INT >= 33) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }

            if (Build.VERSION.SDK_INT >= 37) {
                add(Manifest.permission.ACCESS_LOCAL_NETWORK)
            }
        }.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 9001)
        }
    }
}