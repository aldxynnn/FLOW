package com.aldxynnn.flow

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aldxynnn.flow.core.location.LocationService
import com.aldxynnn.flow.screens.*
import com.aldxynnn.flow.core.network.Trip

private val ShellGreen = Color(0xFF00B14F)
private val ShellGreenDark = Color(0xFF008F3F)
private val ShellGreenSoft = Color(0xFFE8F7EE)

private val ShellInk = Color(0xFF17211B)
private val ShellMuted = Color(0xFF758079)
private val ShellWhite = Color(0xFFFFFFFF)
private val ShellBorder = Color(0xFFE1E7E3)
private val ShellBackground = Color(0xFFF7F9F8)

@Composable
fun FlowApp(vm: FlowViewModel) {
    val session by vm.session.collectAsState()
    val trips by vm.trips.collectAsState()
    val loading by vm.loading.collectAsState()
    val message by vm.message.collectAsState()

    AnimatedContent(
        targetState = session != null,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "auth"
    ) { authenticated ->

        if (!authenticated) {
            LoginScreen(
                loading,
                message,
                vm::login,
                vm::dismissMessage
            )
        } else {
            FlowShell(
                vm,
                trips,
                loading,
                message
            )
        }
    }
}

@Composable
private fun FlowShell(
    vm: FlowViewModel,
    trips: List<Trip>,
    loading: Boolean,
    message: String?
) {
    val nav = rememberNavController()
    val context = LocalContext.current
    val session = vm.session.collectAsState().value
    val role = session?.role.orEmpty()

    val routes = when (role) {
        "DRIVER" -> listOf(
            "home" to "Home",
            "trips" to "Trips",
            "profile" to "Account"
        )

        "DISPATCHER" -> listOf(
            "home" to "Control",
            "trips" to "Trips",
            "profile" to "Account"
        )

        "FLEET_MANAGER" -> listOf(
            "home" to "Overview",
            "trips" to "Trips",
            "profile" to "Account"
        )

        "ADMIN" -> listOf(
            "home" to "Overview",
            "trips" to "Admin",
            "profile" to "Account"
        )

        else -> listOf(
            "home" to "Overview",
            "profile" to "Account"
        )
    }

    var active by remember {
        mutableStateOf("home")
    }

    Scaffold(
        containerColor = ShellBackground,

        bottomBar = {

            FlowBottomBar(
                routes = routes,
                active = active,
                onNavigate = { route ->
                    active = route

                    nav.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }

    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            NavHost(
                navController = nav,
                startDestination = "home"
            ) {

                composable("home") {

                    DashboardScreen(
                        session,
                        trips,
                        vm.summary.collectAsState().value,
                        loading,
                        message,
                        vm::refreshAll,
                        {
                            nav.navigate("trip/${it.id}")
                        },
                        vm::dismissMessage
                    )
                }

                composable("trips") {

                    if (role == "ADMIN") {

                        AdminScreen(
                            users = vm.users.collectAsState().value,
                            trips = trips,
                            vehicles = vm.vehicles.collectAsState().value,
                            drivers = vm.drivers.collectAsState().value,
                            loading = loading,
                            onCreateUser = vm::createUser,
                            onToggleUser = vm::setUserEnabled,
                            onCreateVehicle = vm::createVehicle,
                            onCreateTrip = vm::createTrip,
                            onAssign = vm::assignTrip,
                            onRefresh = vm::refreshAll
                        )

                    } else {

                        TripListScreen(
                            role,
                            trips,
                            loading,
                            vm::refreshAll,
                            {
                                nav.navigate("trip/${it.id}")
                            },
                            vm::assignTrip,
                            vm.drivers.collectAsState().value,
                            vm.vehicles.collectAsState().value,
                            vm::createTrip,
                            vm::createVehicle
                        )
                    }
                }

                composable("profile") {

                    ProfileScreen(
                        session,
                        vm::logout
                    )
                }

                composable(
                    "trip/{tripId}",
                    arguments = listOf(
                        navArgument("tripId") {
                            type = NavType.LongType
                        }
                    )
                ) { entry ->

                    val id =
                        entry.arguments?.getLong("tripId")
                            ?: return@composable

                    trips.firstOrNull {
                        it.id == id
                    }?.let { trip ->

                        TripDetailScreen(
                            trip = trip,
                            role = role,

                            onBack = {
                                nav.popBackStack()
                            },

                            onStart = {
                                vm.startTrip(trip)

                                context.startService(
                                    Intent(
                                        context,
                                        LocationService::class.java
                                    ).putExtra(
                                        LocationService.EXTRA_TRIP_ID,
                                        trip.id
                                    )
                                )
                            },

                            onComplete = {
                                vm.completeTrip(trip)

                                context.stopService(
                                    Intent(
                                        context,
                                        LocationService::class.java
                                    )
                                )
                            },

                            onAssign = { driver ->
                                vm.assignTrip(
                                    trip,
                                    driver
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowBottomBar(
    routes: List<Pair<String, String>>,
    active: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ShellWhite,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            ShellBorder
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = 10.dp,
                    end = 10.dp,
                    top = 8.dp,
                    bottom = 7.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            routes.forEachIndexed { index, (route, label) ->

                val selected = active == route

                FlowBottomItem(
                    modifier = Modifier.weight(1f),
                    label = label,
                    selected = selected,
                    icon = when (index) {
                        0 -> "⌂"
                        1 -> "▦"
                        else -> "●"
                    },
                    onClick = {
                        onNavigate(route)
                    }
                )
            }
        }
    }
}

@Composable
private fun FlowBottomItem(
    modifier: Modifier,
    label: String,
    selected: Boolean,
    icon: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(58.dp)
            .clip(
                RoundedCornerShape(15.dp)
            ),
        onClick = onClick,
        color = if (selected) {
            ShellGreenSoft
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(15.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = icon,
                color = if (selected) {
                    ShellGreenDark
                } else {
                    ShellMuted
                },
                fontSize = 19.sp,
                lineHeight = 20.sp,
                fontWeight = if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = label,
                color = if (selected) {
                    ShellGreenDark
                } else {
                    ShellMuted
                },
                fontSize = 10.sp,
                fontWeight = if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                }
            )
        }
    }
}