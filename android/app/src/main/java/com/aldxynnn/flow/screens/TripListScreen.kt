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

private val TripListGreen = Color(0xFF00B14F)
private val TripListGreenDark = Color(0xFF008F3F)
private val TripListGreenSoft = Color(0xFFE8F7EE)

private val TripListInk = Color(0xFF17211B)
private val TripListInkSoft = Color(0xFF3C4741)
private val TripListMuted = Color(0xFF758079)

private val TripListBackground = Color(0xFFF7F9F8)
private val TripListWhite = Color(0xFFFFFFFF)
private val TripListBorder = Color(0xFFE1E7E3)

@Composable
fun TripListScreen(
    role: String,
    trips: List<Trip>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onOpenTrip: (Trip) -> Unit,
    onAssign: (Trip, String) -> Unit,
    drivers: List<DriverSummary>,
    vehicles: List<Vehicle>,
    onCreateTrip: (CreateTripRequest, (() -> Unit)?) -> Unit,
    onCreateVehicle: (VehicleRequest, (() -> Unit)?) -> Unit
) {
    var showCreate by remember { mutableStateOf(false) }
    var showVehicle by remember { mutableStateOf(false) }
    var assignTrip by remember { mutableStateOf<Trip?>(null) }
    var filter by remember { mutableStateOf("ALL") }

    val canPlan =
        role == "FLEET_MANAGER" || role == "ADMIN"

    val visibleTrips =
        if (filter == "ALL") {
            trips
        } else {
            trips.filter { it.status == filter }
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TripListBackground),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 18.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {

        item {

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TripListWhite,
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(
                        1.dp,
                        TripListBorder
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(13.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(
                                                TripListGreen,
                                                RoundedCornerShape(50)
                                            )
                                    )

                                    Spacer(
                                        modifier = Modifier.width(6.dp)
                                    )

                                    Text(
                                        text = if (
                                            role == "DISPATCHER"
                                        ) {
                                            "OPERATIONS"
                                        } else {
                                            "TRIPS"
                                        },
                                        color = TripListGreenDark,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    )
                                }

                                Text(
                                    text = if (
                                        role == "DISPATCHER"
                                    ) {
                                        "Assignment board"
                                    } else {
                                        "Trip planning"
                                    },
                                    color = TripListInk,
                                    fontSize = 25.sp,
                                    lineHeight = 30.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Monitor every job with clear status and ownership.",
                                    color = TripListMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }

                            Spacer(
                                modifier = Modifier.width(10.dp)
                            )

                            OutlinedButton(
                                onClick = onRefresh,
                                enabled = !loading,
                                modifier = Modifier.height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    TripListBorder
                                ),
                                contentPadding = PaddingValues(
                                    horizontal = 13.dp
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = TripListInk
                                )
                            ) {
                                Text(
                                    text = if (loading) {
                                        "Syncing…"
                                    } else {
                                        "Refresh"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (canPlan) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                Button(
                                    onClick = {
                                        showCreate = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TripListGreen,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = "New trip",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        showVehicle = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        TripListBorder
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = TripListInk
                                    )
                                ) {
                                    Text(
                                        text = "Vehicle",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Filter
                TripFilterBar(
                    filter = filter,
                    onFilterChange = {
                        filter = it
                    }
                )
            }
        }

        if (visibleTrips.isEmpty()) {

            item {
                EmptyState(role)
            }

        } else {

            items(
                visibleTrips,
                key = { it.id }
            ) { trip ->

                TripListCard(
                    trip = trip,
                    role = role,
                    onOpenTrip = {
                        onOpenTrip(trip)
                    },
                    onAssign = {
                        assignTrip = trip
                    }
                )
            }
        }
    }

    if (showVehicle) {
        CreateVehicleDialog(
            { showVehicle = false }
        ) { request ->
            onCreateVehicle(request) {
                showVehicle = false
            }
        }
    }

    if (showCreate) {
        CreateTripDialog(
            drivers,
            vehicles,
            { showCreate = false }
        ) { request ->
            onCreateTrip(request) {
                showCreate = false
            }
        }
    }

    assignTrip?.let { trip ->

        AssignDialog(
            trip,
            drivers,
            { assignTrip = null }
        ) { driver ->

            onAssign(
                trip,
                driver
            )

            assignTrip = null
        }
    }
}

@Composable
private fun TripFilterBar(
    filter: String,
    onFilterChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TripListWhite,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            TripListBorder
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            listOf(
                "ALL",
                "ASSIGNED",
                "IN_PROGRESS",
                "COMPLETED"
            ).forEach { state ->

                val selected = filter == state

                Surface(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onFilterChange(state)
                    },
                    color = if (selected) {
                        TripListGreenSoft
                    } else {
                        Color.Transparent
                    },
                    shape = RoundedCornerShape(11.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 5.dp,
                                vertical = 9.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = state.replace(
                                '_',
                                ' '
                            ),
                            color = if (selected) {
                                TripListGreenDark
                            } else {
                                TripListMuted
                            },
                            fontSize = 9.sp,
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
}

@Composable
private fun TripListCard(
    trip: Trip,
    role: String,
    onOpenTrip: () -> Unit,
    onAssign: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpenTrip,
        color = TripListWhite,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            TripListBorder
        )
    ) {

        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = trip.code,
                    modifier = Modifier.weight(1f),
                    color = TripListGreenDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                TripStatusPill(
                    status = trip.status
                )
            }

            Text(
                text = "${trip.origin} → ${trip.destination}",
                color = TripListInk,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(
                color = TripListBorder
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                TripMeta(
                    label = "DRIVER",
                    value = trip.driverUsername,
                    modifier = Modifier.weight(1f)
                )

                TripMeta(
                    label = "VEHICLE",
                    value = trip.vehiclePlate,
                    modifier = Modifier.weight(1f)
                )

                TripMeta(
                    label = "ETA",
                    value = "${trip.etaMinutes} min",
                    modifier = Modifier.weight(0.7f)
                )
            }

            if (
                role == "DISPATCHER" &&
                trip.status != "COMPLETED"
            ) {

                OutlinedButton(
                    onClick = onAssign,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(43.dp),
                    shape = RoundedCornerShape(11.dp),
                    border = BorderStroke(
                        1.dp,
                        TripListBorder
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TripListInk
                    )
                ) {
                    Text(
                        text = "Assign / reassign",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TripMeta(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {

        Text(
            text = label,
            color = TripListMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.7.sp
        )

        Text(
            text = value,
            color = TripListInkSoft,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun TripStatusPill(
    status: String
) {
    val background = when (status) {
        "IN_PROGRESS" -> TripListGreenSoft
        "COMPLETED" -> Color(0xFFF0F2F1)
        "CANCELLED" -> Color(0xFFFCEDEC)
        else -> Color(0xFFFFF5E3)
    }

    val content = when (status) {
        "IN_PROGRESS" -> TripListGreenDark
        "COMPLETED" -> TripListMuted
        "CANCELLED" -> Color(0xFFB33B34)
        else -> Color(0xFFB97816)
    }

    Surface(
        color = background,
        shape = RoundedCornerShape(50)
    ) {

        Text(
            text = status.replace(
                '_',
                ' '
            ),
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 6.dp
            ),
            color = content,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
fun EmptyState(
    role: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TripListWhite,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            TripListBorder
        )
    ) {

        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            TripListGreen,
                            RoundedCornerShape(50)
                        )
                )

                Spacer(
                    modifier = Modifier.width(7.dp)
                )

                Text(
                    text = "TRIP BOARD",
                    color = TripListGreenDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "Nothing here yet",
                color = TripListInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (role == "DRIVER") {
                    "Wait for dispatch to assign your next trip."
                } else {
                    "Create your first operational record to start seeing live data."
                },
                color = TripListMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}