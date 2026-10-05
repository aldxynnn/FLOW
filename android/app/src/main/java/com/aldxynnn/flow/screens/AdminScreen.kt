package com.aldxynnn.flow.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aldxynnn.flow.core.network.*
import com.aldxynnn.flow.ui.*

private val AdminGreen = Color(0xFF0B7A55)
private val AdminGreenDark = Color(0xFF07583E)
private val AdminGreenSoft = Color(0xFFE3F2EC)

private val AdminInk = Color(0xFF112F25)
private val AdminInkSoft = Color(0xFF385149)
private val AdminMuted = Color(0xFF66756E)

private val AdminBackground = Color(0xFFF4F7F5)
private val AdminWhite = Color(0xFFFFFFFF)
private val AdminBorder = Color(0xFFD9E3DE)

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
        shape = RoundedCornerShape(16.dp),
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
                                Color(0xFFC48700)
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
        shape = RoundedCornerShape(16.dp),
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
        shape = RoundedCornerShape(16.dp),
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
        shape = RoundedCornerShape(16.dp),
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
                    Color(0xFFEEF1EF)
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
        shape = RoundedCornerShape(16.dp),
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
        shape = RoundedCornerShape(16.dp),
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
private fun AdminFormDialog(
    eyebrow: String,
    title: String,
    subtitle: String,
    confirmLabel: String,
    confirmEnabled: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(
                    horizontal = 8.dp,
                    vertical = 18.dp
                )
                .heightIn(max = 720.dp),
            color = AdminWhite,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, AdminBorder),
            shadowElevation = 14.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = eyebrow,
                            color = AdminGreenDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )

                        Text(
                            text = title,
                            color = AdminInk,
                            fontSize = 22.sp,
                            lineHeight = 27.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = subtitle,
                            color = AdminMuted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "×",
                            color = AdminMuted,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                HorizontalDivider(
                    color = AdminBorder
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 455.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                    content = content
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(13.dp),
                        border = BorderStroke(1.dp, AdminBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AdminInkSoft
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = confirmEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AdminGreen,
                            contentColor = Color.White,
                            disabledContainerColor = AdminGreen.copy(alpha = 0.12f),
                            disabledContentColor = AdminMuted
                        )
                    ) {
                        Text(
                            text = confirmLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = {
            Text(
                text = label,
                fontSize = 12.sp
            )
        },
        placeholder = placeholder?.let { hint ->
            {
                Text(
                    text = hint,
                    color = AdminMuted.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        },
        visualTransformation = if (isPassword) {
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF8FAF9),
            unfocusedContainerColor = Color(0xFFF8FAF9),
            focusedBorderColor = AdminGreen,
            unfocusedBorderColor = AdminBorder,
            focusedLabelColor = AdminGreenDark,
            unfocusedLabelColor = AdminMuted,
            focusedTextColor = AdminInk,
            unfocusedTextColor = AdminInk,
            cursorColor = AdminGreen
        )
    )
}

@Composable
private fun AdminSectionLabel(
    text: String,
    supporting: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = text,
            color = AdminInk,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        supporting?.let {
            Text(
                text = it,
                color = AdminMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun AdminChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    supporting: String? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = if (selected) {
            AdminGreenSoft
        } else {
            Color(0xFFF8FAF9)
        },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (selected) {
                AdminGreen.copy(alpha = 0.45f)
            } else {
                AdminBorder
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 13.dp,
                vertical = 11.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        color = if (selected) {
                            AdminGreen
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(50)
                    )
                    .then(
                        if (!selected) {
                            Modifier.border(
                                1.dp,
                                AdminBorder,
                                RoundedCornerShape(50)
                            )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(11.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    color = AdminInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                supporting?.let {
                    Text(
                        text = it,
                        color = AdminMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
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

    AdminFormDialog(
        eyebrow = "NEW ACCOUNT",
        title = "Create user",
        subtitle = "Set up an operational account and its access level.",
        confirmLabel = "Create user",
        confirmEnabled = username.isNotBlank() && password.length >= 8,
        onDismiss = onDismiss,
        onConfirm = {
            onCreate(
                CreateUserRequest(
                    username.trim(),
                    password,
                    role
                )
            )
        }
    ) {
        AdminFormField(
            value = username,
            onValueChange = { username = it },
            label = "Username",
            placeholder = "e.g. driver01"
        )

        AdminFormField(
            value = password,
            onValueChange = { password = it },
            label = "Temporary password",
            placeholder = "Minimum 8 characters",
            isPassword = true
        )

        AdminSectionLabel(
            text = "Access role",
            supporting = "Choose the level of access this account should have."
        )

        listOf(
            "DRIVER" to "Access own assigned trips and live driving tools.",
            "DISPATCHER" to "Manage assignments and monitor operations.",
            "FLEET_MANAGER" to "Plan trips and oversee fleet activity.",
            "ADMIN" to "Manage accounts, fleet and operational settings."
        ).forEach { (value, description) ->
            AdminChoiceRow(
                label = value.replace('_', ' '),
                supporting = description,
                selected = role == value,
                onClick = { role = value }
            )
        }
    }
}

@Composable
fun CreateVehicleDialog(
    onDismiss: () -> Unit,
    onCreate: (VehicleRequest) -> Unit
) {
    var plate by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }

    AdminFormDialog(
        eyebrow = "FLEET",
        title = "Add vehicle",
        subtitle = "Register a vehicle so it can be assigned to operational work.",
        confirmLabel = "Add vehicle",
        confirmEnabled = plate.isNotBlank() &&
            model.isNotBlank() &&
            type.isNotBlank(),
        onDismiss = onDismiss,
        onConfirm = {
            onCreate(
                VehicleRequest(
                    plate.trim(),
                    model.trim(),
                    type.trim()
                )
            )
        }
    ) {
        AdminFormField(
            value = plate,
            onValueChange = { plate = it.uppercase() },
            label = "Plate number",
            placeholder = "e.g. B 1234 FLOW"
        )

        AdminFormField(
            value = model,
            onValueChange = { model = it },
            label = "Model",
            placeholder = "e.g. Toyota Innova"
        )

        AdminFormField(
            value = type,
            onValueChange = { type = it },
            label = "Vehicle type",
            placeholder = "e.g. MPV"
        )

        Surface(
            color = AdminGreenSoft.copy(alpha = 0.6f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "The vehicle starts as AVAILABLE and becomes ON TRIP when assigned to active work.",
                color = AdminGreenDark,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
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

    val availableDrivers = drivers.filter { it.enabled }
    val availableVehicles = vehicles.filter {
        it.status == "AVAILABLE"
    }

    AdminFormDialog(
        eyebrow = "DISPATCH",
        title = "Create trip",
        subtitle = "Build a route, then assign an available driver and vehicle.",
        confirmLabel = "Create trip",
        confirmEnabled = origin.isNotBlank() &&
            destination.isNotBlank() &&
            driver != null &&
            vehicle != null,
        onDismiss = onDismiss,
        onConfirm = {
            onCreate(
                CreateTripRequest(
                    origin.trim(),
                    destination.trim(),
                    vehicle.orEmpty(),
                    driver.orEmpty(),
                    eta.toIntOrNull() ?: 0
                )
            )
        }
    ) {
        AdminFormField(
            value = origin,
            onValueChange = { origin = it },
            label = "Origin",
            placeholder = "Starting point"
        )

        AdminFormField(
            value = destination,
            onValueChange = { destination = it },
            label = "Destination",
            placeholder = "Final destination"
        )

        AdminFormField(
            value = eta,
            onValueChange = {
                eta = it.filter(Char::isDigit)
            },
            label = "ETA",
            placeholder = "Minutes"
        )

        AdminSectionLabel(
            text = "Driver",
            supporting = if (availableDrivers.isEmpty()) {
                "No enabled drivers are available right now."
            } else {
                "Select the driver responsible for this trip."
            }
        )

        availableDrivers.forEach { item ->
            AdminChoiceRow(
                label = item.username,
                supporting = "Enabled driver",
                selected = driver == item.username,
                onClick = { driver = item.username }
            )
        }

        AdminSectionLabel(
            text = "Vehicle",
            supporting = if (availableVehicles.isEmpty()) {
                "No vehicles are currently available."
            } else {
                "Select an AVAILABLE vehicle."
            }
        )

        availableVehicles.forEach { item ->
            AdminChoiceRow(
                label = item.plateNumber,
                supporting = "${item.model} · ${item.type}",
                selected = vehicle == item.plateNumber,
                onClick = { vehicle = item.plateNumber }
            )
        }
    }
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
            trip.driverUsername.ifBlank { null }
        )
    }

    val availableDrivers = drivers.filter { it.enabled }

    AdminFormDialog(
        eyebrow = "ASSIGNMENT",
        title = "Assign ${trip.code}",
        subtitle = "Choose the enabled driver who owns this work.",
        confirmLabel = "Save assignment",
        confirmEnabled = selected != null,
        onDismiss = onDismiss,
        onConfirm = {
            selected?.let(onAssign)
        }
    ) {
        Surface(
            color = Color(0xFFF8FAF9),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(
                modifier = Modifier.padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${trip.origin} → ${trip.destination}",
                    color = AdminInk,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${trip.vehiclePlate} · ${trip.status.replace('_', ' ')}",
                    color = AdminMuted,
                    fontSize = 11.sp
                )
            }
        }

        AdminSectionLabel(
            text = "Driver",
            supporting = "Changing the assignment updates who owns this trip."
        )

        availableDrivers.forEach { driver ->
            AdminChoiceRow(
                label = driver.username,
                supporting = "Enabled driver",
                selected = selected == driver.username,
                onClick = {
                    selected = driver.username
                }
            )
        }

        if (availableDrivers.isEmpty()) {
            Text(
                text = "There are no enabled drivers available for assignment.",
                color = AdminMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun StatusPill(
    status: String
) {
    val container = when (status) {
        "IN_PROGRESS" -> AdminGreenSoft
        "COMPLETED" -> Color(0xFFEEF1EF)
        "CANCELLED" -> SoftRed
        else -> Color(0xFFEEF4F1)
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