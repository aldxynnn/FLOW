package com.aldxynnn.flow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.aldxynnn.flow.ui.theme.FLOWDriverTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FLOWDriverTheme {
                AppRoot()
            }
        }
    }
}