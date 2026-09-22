package com.aldxynnn.flow

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val AUTH_ROUTE = "auth"

@Composable
fun AppRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AUTH_ROUTE
    ) {
        composable(AUTH_ROUTE) {
            AuthPlaceholderScreen()
        }
    }
}

@Composable
private fun AuthPlaceholderScreen() {
    // Authentication UI akan dibuat pada task berikutnya.
}