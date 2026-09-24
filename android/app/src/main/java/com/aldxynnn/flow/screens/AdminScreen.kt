package com.aldxynnn.flow.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldxynnn.flow.core.network.*
import com.aldxynnn.flow.ui.*

private val AdminGreen = Color(0xFF00B14F)
private val AdminGreenDark = Color(0xFF008F3F)
private val AdminGreenSoft = Color(0xFFE8F7EE)

private val AdminInk = Color(0xFF17211B)
private val AdminInkSoft = Color(0xFF3C4741)
private val AdminMuted = Color(0xFF758079)

private val AdminBackground = Color(0xFFF7F9F8)
private val AdminWhite = Color(0xFFFFFFFF)
private val AdminBorder = Color(0xFFE1E7E3)

@Composable
fun AdminScreen(
    users: List<UserSummary>,
    trips: List<Trip>,
    vehicles: List<Vehicle>,
    drivers: List<DriverSummary>,
    loading: Boolean,
    onCreateUser: (CreateUserRequest, (() -> Unit)?) -> Unit,
    onToggleUser: (UserSummary) -> Unit,
    onCreateVehicle: (VehicleRequest, (() -> Unit)?) -> Unit,
    onCreateTrip: (CreateTripRequest, (() -> Unit)?) -> Unit,
    onAssign: (Trip, String) -> Unit,
    onRefresh: () -> Unit
) {
    var tab by remember { mutableStateOf(0) }
    var createUser by remember { mutableStateOf(false) }
    var createVehicle by remember { mutableStateOf(false) }
    var createTrip by remember { mutableStateOf(false) }
    var assignTrip by remember { mutableStateOf<Trip?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBackground),
        contentPadding = PaddingValues(
            start = 18.dp,
            top = 18.dp,
            end = 18.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {
            AdminHeader(
                loading = loading,
                onRefresh = onRefresh
            )
        }

        item {
            AdminTabs(
                selectedTab = tab,
                onTabSelected = { tab = it }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                AdminMetric(
                    label = "Users",
                    value = users.size.toString(),
                    iconText = "01",
                    modifier = Modifier.weight(1f)
                )

                AdminMetric(
                    label = "Vehicles",
                    value = vehicles.size.toString(),
                    iconText = "02",
                    modifier = Modifier.weight(1f)
                )

                AdminMetric(
                    label = "Trips",
                    value = trips.size.toString(),
                    iconText = "03",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when (tab) {

            0 -> {
                item {
                    SectionAction(
                        title = "People & access",
                        subtitle = "Manage operational accounts and permissions.",
                        action = "New user"
                    ) {
                        createUser = true
                    }
                }

                items(
                    users,
                    key = { it.id }
                ) { user ->
                    UserCard(
                        user = user,
                        onToggle = {
                            onToggleUser(user)
                        }
                    )
                }

                if (users.isEmpty()) {
                    item {
                        EmptyState("ADMIN")
                    }
                }
            }

            1 -> {
                item {
                    SectionAction(
                        title = "Fleet",
                        subtitle = "Keep your available vehicles up to date.",
                        action = "Add vehicle"
                    ) {
                        createVehicle = true
                    }
                }

                items(
                    vehicles,
                    key = { it.id }
                ) { vehicle ->
                    VehicleCard(vehicle)
                }

                if (vehicles.isEmpty()) {
                    item {
                        EmptyState("ADMIN")
                    }
                }
            }

            else -> {
                item {
                    SectionAction(
                        title = "Operations",
                        subtitle = "Create trips and manage current assignments.",
                        action = "New trip"
                    ) {
                        createTrip = true
                    }
                }

                items(
                    trips,
                    key = { it.id }
                ) { trip ->
                    TripAdminCard(
                        trip = trip,
                        onAssign = {
                            assignTrip = trip
                        }
                    )
                }

                if (trips.isEmpty()) {
                    item {
                        EmptyState("ADMIN")
                    }
                }
            }
        }
    }

    if (createUser) {
        CreateUserDialog(
            onDismiss = {
                createUser = false
            }
        ) { request ->
            onCreateUser(request) {
                createUser = false
            }
        }
    }

    if (createVehicle) {
        CreateVehicleDialog(
            onDismiss = {
                createVehicle = false
            }
        ) { request ->
            onCreateVehicle(request) {
                createVehicle = false
            }
        }
    }

    if (createTrip) {
        CreateTripDialog(
            drivers = drivers,
            vehicles = vehicles,
            onDismiss = {
                createTrip = false
            }
        ) { request ->
            onCreateTrip(request) {
                createTrip = false
            }
        }
    }

    assignTrip?.let { trip ->
        AssignDialog(
            trip = trip,
            drivers = drivers,
            onDismiss = {
                assignTrip = null
            }
        ) { driver ->
            onAssign(trip, driver)
            assignTrip = null
        }
    }
}

@Composable
private fun AdminHeader(
    loading: Boolean,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AdminWhite,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.dp,
            AdminBorder
        )
    ) {

        Column(
            modifier = Modifier.padding(19.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    AdminGreen,
                                    RoundedCornerShape(50)
                                )
                        )

                        Spacer(
                            modifier = Modifier.width(7.dp)
                        )

                        Text(
                            text = "FLOW",
                            color = AdminGreenDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(
                            text = "•",
                            color = AdminMuted,
                            fontSize = 11.sp
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(
                            text = "ADMIN",
                            color = AdminMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Text(
                        text = "Administration",
                        color = AdminInk,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Manage people, fleet and daily operations.",
                        color = AdminMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AdminGreenSoft
                ) {

                    IconButton(
                        onClick = onRefresh
                    ) {
                        Text(
                            text = "↻",
                            color = AdminGreenDark,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            if (loading) {
                                Color(0xFFE0A400)
                            } else {
                                AdminGreen
                            },
                            RoundedCornerShape(50)
                        )
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = if (loading) {
                        "Syncing operational data…"
                    } else {
                        "Workspace synced"
                    },
                    color = AdminMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AdminTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        listOf(
            "People",
            "Fleet",
            "Trips"
        ).forEachIndexed { index, label ->

            val selected = selectedTab == index

            Surface(
                modifier = Modifier.weight(1f),
                onClick = {
                    onTabSelected(index)
                },
                shape = RoundedCornerShape(13.dp),
                color = if (selected) {
                    AdminGreen
                } else {
                    AdminWhite
                },
                border = if (selected) {
                    null
                } else {
                    BorderStroke(
                        1.dp,
                        AdminBorder
                    )
                }
            ) {

                Box(
                    modifier = Modifier.padding(
                        vertical = 11.dp
                    ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = label,
                        color = if (selected) {
                            Color.White
                        } else {
                            AdminInkSoft
                        },
                        fontSize = 13.sp,
                        fontWeight = if (selected) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminMetric(
    label: String,
    value: String,
    iconText: String,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        color = AdminWhite,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            AdminBorder
        )
    ) {

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = label,
                    color = AdminMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = iconText,
                    color = AdminGreenDark,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = value,
                color = AdminInk,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionAction(
    title: String,
    subtitle: String,
    action: String,
    onAction: () -> Unit
) {
    Surface(
        color = AdminWhite,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            AdminBorder
        )
    ) {

        Row(
            modifier = Modifier.padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = title,
                    color = AdminInk,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    color = AdminMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Button(
                onClick = onAction,
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AdminGreen,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(
                    horizontal = 14.dp,
                    vertical = 9.dp
                )
            ) {
                Text(
                    text = action,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun UserCard(
    user: UserSummary,
    onToggle: () -> Unit
) {
    Surface(
        color = AdminWhite,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            AdminBorder
        )
    ) {

        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                color = AdminGreenSoft,
                shape = RoundedCornerShape(11.dp)
            ) {

                Text(
                    text = user.username
                        .take(1)
                        .uppercase(),
                    color = AdminGreenDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 10.dp
                    )
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {

                Text(
                    text = user.username,
                    color = AdminInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Text(
                    text = user.role.replace('_', ' '),
                    color = AdminMuted,
                    fontSize = 11.sp
                )
            }

            Surface(
                onClick = onToggle,
                shape = RoundedCornerShape(50),
                color = if (user.enabled) {
                    AdminGreenSoft
                } else {
                    Color(0xFFF0F2F1)
                }
            ) {

                Text(
                    text = if (user.enabled) {
                        "Enabled"
                    } else {
                        "Disabled"
                    },
                    color = if (user.enabled) {
                        AdminGreenDark
                    } else {
                        AdminMuted
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 7.dp
                    )
                )
            }
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle
) {
    Surface(
        color = AdminWhite,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            AdminBorder
        )
    ) {

        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                color = AdminGreenSoft,
                shape = RoundedCornerShape(11.dp)
            ) {

                Text(
                    text = "CAR",
                    color = AdminGreenDark,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 10.dp
                    )
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {

                Text(
                    text = vehicle.plateNumber,
                    color = AdminInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Text(
                    text = "${vehicle.model} · ${vehicle.type}",
                    color = AdminMuted,
                    fontSize = 11.sp
                )
            }

            StatusPill(vehicle.status)
        }
    }
}

@Composable
private fun TripAdminCard(
    trip: Trip,
    onAssign: () -> Unit
) {
    Surface(
        color = AdminWhite,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            AdminBorder
        )
    ) {

        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = trip.code,
                    color = AdminGreenDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )

                StatusPill(trip.status)
            }

            Text(
                text = "${trip.origin} → ${trip.destination}",
                color = AdminInk,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${trip.driverUsername} · ${trip.vehiclePlate}",
                color = AdminMuted,
                fontSize = 11.sp
            )

            if (trip.status != "COMPLETED") {

                OutlinedButton(
                    onClick = onAssign,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        AdminBorder
                    )
                ) {
                    Text(
                        text = "Assign / reassign",
                        color = AdminInkSoft,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CreateUserDialog(
    onDismiss: () -> Unit,
    onCreate: (CreateUserRequest) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("DRIVER") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create account")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OutlinedTextField(
                    username,
                    { username = it },
                    label = {
                        Text("Username")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    password,
                    { password = it },
                    label = {
                        Text("Temporary password")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Role",
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    listOf(
                        "DRIVER",
                        "DISPATCHER",
                        "FLEET_MANAGER",
                        "ADMIN"
                    ).forEach { r ->

                        FilterChip(
                            selected = role == r,
                            onClick = {
                                role = r
                            },
                            label = {
                                Text(
                                    r.replace('_', ' ')
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(
                        CreateUserRequest(
                            username.trim(),
                            password,
                            role
                        )
                    )
                },
                enabled = username.isNotBlank() &&
                        password.length >= 8,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AdminGreen
                )
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateVehicleDialog(
    onDismiss: () -> Unit,
    onCreate: (VehicleRequest) -> Unit
) {
    var plate by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add vehicle")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {

                OutlinedTextField(
                    plate,
                    { plate = it },
                    label = {
                        Text("Plate number")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    model,
                    { model = it },
                    label = {
                        Text("Model")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    type,
                    { type = it },
                    label = {
                        Text("Type")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(
                        VehicleRequest(
                            plate.trim(),
                            model.trim(),
                            type.trim()
                        )
                    )
                },
                enabled = plate.isNotBlank() &&
                        model.isNotBlank() &&
                        type.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AdminGreen
                )
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateTripDialog(
    drivers: List<DriverSummary>,
    vehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    onCreate: (CreateTripRequest) -> Unit
) {
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var eta by remember { mutableStateOf("30") }
    var driver by remember { mutableStateOf<String?>(null) }
    var vehicle by remember { mutableStateOf<String?>(null) }

    val availableDrivers =
        drivers.filter {
            it.enabled
        }

    val availableVehicles =
        vehicles.filter {
            it.status == "AVAILABLE"
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create trip")
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.heightIn(
                    max = 430.dp
                )
            ) {

                item {
                    OutlinedTextField(
                        origin,
                        { origin = it },
                        label = {
                            Text("Origin")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        destination,
                        { destination = it },
                        label = {
                            Text("Destination")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        eta,
                        {
                            eta = it.filter(Char::isDigit)
                        },
                        label = {
                            Text("ETA minutes")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        "Driver",
                        fontWeight = FontWeight.Bold
                    )
                }

                items(
                    availableDrivers,
                    key = { it.username }
                ) { d ->

                    FilterChip(
                        selected = driver == d.username,
                        onClick = {
                            driver = d.username
                        },
                        label = {
                            Text(d.username)
                        }
                    )
                }

                item {
                    Text(
                        "Vehicle",
                        fontWeight = FontWeight.Bold
                    )
                }

                items(
                    availableVehicles,
                    key = { it.id }
                ) { v ->

                    FilterChip(
                        selected = vehicle == v.plateNumber,
                        onClick = {
                            vehicle = v.plateNumber
                        },
                        label = {
                            Text(
                                "${v.plateNumber} · ${v.model}"
                            )
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(
                        CreateTripRequest(
                            origin.trim(),
                            destination.trim(),
                            vehicle.orEmpty(),
                            driver.orEmpty(),
                            eta.toIntOrNull() ?: 0
                        )
                    )
                },
                enabled = origin.isNotBlank() &&
                        destination.isNotBlank() &&
                        driver != null &&
                        vehicle != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AdminGreen
                )
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AssignDialog(
    trip: Trip,
    drivers: List<DriverSummary>,
    onDismiss: () -> Unit,
    onAssign: (String) -> Unit
) {
    var selected by remember {
        mutableStateOf<String?>(
            trip.driverUsername.ifBlank {
                null
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Assign ${trip.code}")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    "Choose an enabled driver.",
                    color = AdminMuted
                )

                drivers
                    .filter {
                        it.enabled
                    }
                    .forEach { driver ->

                        FilterChip(
                            selected = selected == driver.username,
                            onClick = {
                                selected = driver.username
                            },
                            label = {
                                Text(driver.username)
                            }
                        )
                    }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selected?.let(onAssign)
                },
                enabled = selected != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AdminGreen
                )
            ) {
                Text("Save assignment")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StatusPill(
    status: String
) {
    val container = when (status) {
        "IN_PROGRESS" -> AdminGreenSoft
        "COMPLETED" -> Color(0xFFF0F2F1)
        "CANCELLED" -> SoftRed
        else -> Color(0xFFF0F5F2)
    }

    val content = when (status) {
        "IN_PROGRESS" -> AdminGreenDark
        "COMPLETED" -> AdminMuted
        "CANCELLED" -> FlowRed
        else -> AdminGreenDark
    }

    Surface(
        color = container,
        shape = RoundedCornerShape(50)
    ) {

        Text(
            text = status.replace('_', ' '),
            color = content,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 6.dp
            )
        )
    }
}