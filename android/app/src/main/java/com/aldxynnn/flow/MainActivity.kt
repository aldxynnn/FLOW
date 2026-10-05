package com.aldxynnn.flow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aldxynnn.flow.screens.FlowSplashScreen
import com.aldxynnn.flow.ui.FlowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        /*
         * Android system splash.
         *
         * IMPORTANT:
         * This must be called before super.onCreate().
         */
        val splashScreen = installSplashScreen()

        /*
         * Remove the Android starting window without adding a
         * second exit animation. FlowSplashScreen starts with
         * the exact same logo centered on the same background.
         */
        splashScreen.setOnExitAnimationListener { splashViewProvider ->
            splashViewProvider.remove()
        }

        super.onCreate(savedInstanceState)

        val app = application as FlowApplication

        setContent {

            FlowTheme {

                var showSplash by remember {
                    mutableStateOf(true)
                }

                val vm: FlowViewModel = viewModel(
                    factory = FlowViewModelFactory(
                        app.repository,
                        app.sessionStore
                    )
                )

                if (showSplash) {

                    /*
                     * This is the ONLY visible FLOW splash.
                     *
                     * The Android starting splash above it has
                     * a transparent icon, so the user does not
                     * see a second "F" splash.
                     */
                    FlowSplashScreen(
                        onFinished = {

                            /*
                             * Navigation happens immediately
                             * when the truck reaches its destination.
                             */
                            showSplash = false

                            requestFlowPermissions()
                        }
                    )

                } else {

                    FlowApp(vm)
                }
            }
        }
    }

    private fun requestFlowPermissions() {

        val permissions = buildList {

            add(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            add(
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            if (Build.VERSION.SDK_INT >= 33) {

                add(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }

            if (Build.VERSION.SDK_INT >= 37) {

                add(
                    Manifest.permission.ACCESS_LOCAL_NETWORK
                )
            }

        }.filter {

            ActivityCompat.checkSelfPermission(
                this,
                it
            ) != PackageManager.PERMISSION_GRANTED

        }

        if (permissions.isNotEmpty()) {

            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                9001
            )
        }
    }
}