package com.aldxynnn.flow

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.PersonOutline
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aldxynnn.flow.core.location.LocationService
import com.aldxynnn.flow.core.network.Trip
import com.aldxynnn.flow.screens.*
import kotlinx.coroutines.delay

private val ShellGreen = Color(0xFF0B7A55)
private val ShellGreenDark = Color(0xFF07583E)
private val ShellGreenSoft = Color(0xFFE3F2EC)
private val ShellInk = Color(0xFF112F25)
private val ShellMuted = Color(0xFF66756E)
private val ShellWhite = Color(0xFFFFFFFF)
private val ShellBorder = Color(0xFFD9E3DE)
private val ShellBackground = Color(0xFFF4F7F5)

@Composable
fun FlowApp(vm: FlowViewModel) {
    val session by vm.session.collectAsState()
    val trips by vm.trips.collectAsState()
    val loading by vm.loading.collectAsState()
    val message by vm.message.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

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
                    vm = vm,
                    trips = trips,
                    loading = loading
                )
            }
        }

        if (session != null) {
            message
                ?.takeIf { it.isNotBlank() }
                ?.let { text ->

                    FlowActionNotice(
                        modifier = Modifier.align(
                            Alignment.BottomCenter
                        ),
                        message = text,
                        onDismiss = {
                            vm.dismissMessage()
                        }
                    )
                }
        }
    }
}

@Composable
private fun FlowActionNotice(
    modifier: Modifier = Modifier,
    message: String,
    onDismiss: () -> Unit
) {
    var visible by remember(message) {
        mutableStateOf(true)
    }

    LaunchedEffect(message) {
        delay(3000)

        visible = false

        delay(220)

        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier.fillMaxWidth(),
        enter = slideInVertically(
            initialOffsetY = { -it }
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it }
        ) + fadeOut()
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 10.dp
                )
                .navigationBarsPadding(),
            shape = RoundedCornerShape(16.dp),
            color = ShellWhite,
            border = BorderStroke(
                1.dp,
                ShellBorder
            ),
            shadowElevation = 8.dp,
            tonalElevation = 0.dp
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 14.dp,
                        end = 8.dp,
                        top = 11.dp,
                        bottom = 11.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ShellGreenSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = ShellGreenDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.width(11.dp)
                )

                Text(
                    text = message,
                    modifier = Modifier.weight(1f),
                    color = ShellInk,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium
                )

                TextButton(
                    onClick = {
                        visible = false
                        onDismiss()
                    },
                    contentPadding = PaddingValues(
                        horizontal = 8.dp
                    )
                ) {
                    Text(
                        text = "×",
                        color = ShellMuted,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun startLocationTracking(
    context: Context,
    tripId: Long
) {
    val intent = Intent(
        context,
        LocationService::class.java
    ).putExtra(
        LocationService.EXTRA_TRIP_ID,
        tripId
    )

    ContextCompat.startForegroundService(
        context,
        intent
    )
}

private fun stopLocationTracking(
    context: Context
) {
    context.startService(
        Intent(
            context,
            LocationService::class.java
        ).setAction(
            LocationService.ACTION_STOP
        )
    )
}

@Composable
private fun FlowShell(
    vm: FlowViewModel,
    trips: List<Trip>,
    loading: Boolean
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
                        null,
                        vm::refreshAll,
                        {
                            nav.navigate("trip/${it.id}")
                        },
                        {}
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

                    LaunchedEffect(id) {
                        vm.loadTripLocations(id)
                    }

                    trips.firstOrNull {
                        it.id == id
                    }?.let { trip ->

                        LaunchedEffect(
                            trip.id,
                            trip.status,
                            role
                        ) {
                            if (
                                role == "DRIVER" &&
                                trip.status == "IN_PROGRESS"
                            ) {
                                startLocationTracking(
                                    context,
                                    trip.id
                                )
                            }

                            if (
                                role == "DRIVER" &&
                                trip.status == "COMPLETED"
                            ) {
                                stopLocationTracking(
                                    context
                                )
                            }
                        }

                        val locations =
                            vm.tripLocations
                                .collectAsState()
                                .value[id]
                                .orEmpty()

                        TripDetailScreen(
                            trip = trip,
                            role = role,

                            onBack = {
                                nav.popBackStack()
                            },

                            onStart = {
                                vm.startTrip(
                                    trip = trip,
                                    onTrackingReady = {
                                        startLocationTracking(
                                            context,
                                            trip.id
                                        )
                                    }
                                )
                            },

                            onComplete = {
                                vm.completeTrip(
                                    trip = trip,
                                    onTrackingStopped = {
                                        stopLocationTracking(
                                            context
                                        )
                                    }
                                )
                            },

                            locations = locations,

                            actionLoading = loading,

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
        border = BorderStroke(1.dp, ShellBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            routes.forEach { (route, label) ->
                FlowBottomItem(
                    modifier = Modifier.weight(1f),
                    route = route,
                    label = label,
                    selected = active == route,
                    onClick = { onNavigate(route) }
                )
            }
        }
    }
}

@Composable
private fun FlowBottomItem(
    modifier: Modifier,
    route: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (route) {
        "home" -> if (label == "Home") Icons.Outlined.Home else Icons.Outlined.Dashboard
        "trips" -> Icons.Outlined.ListAlt
        else -> Icons.Outlined.PersonOutline
    }

    Surface(
        modifier = modifier.height(58.dp),
        onClick = onClick,
        color = if (selected) ShellGreenSoft else Color.Transparent,
        shape = RoundedCornerShape(13.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) ShellGreenDark else ShellMuted,
                modifier = Modifier.size(21.dp)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = label,
                color = if (selected) ShellGreenDark else ShellMuted,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
