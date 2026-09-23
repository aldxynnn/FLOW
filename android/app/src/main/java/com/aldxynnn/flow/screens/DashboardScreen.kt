package com.aldxynnn.flow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldxynnn.flow.core.network.DashboardSummary
import com.aldxynnn.flow.core.network.Trip
import com.aldxynnn.flow.core.session.Session
import com.aldxynnn.flow.ui.*

private val DashboardGreen = Color(0xFF00B14F)
private val DashboardGreenDark = Color(0xFF008F3F)
private val DashboardGreenSoft = Color(0xFFE8F7EE)

private val DashboardInk = Color(0xFF17211B)
private val DashboardInkSoft = Color(0xFF3C4741)
private val DashboardMuted = Color(0xFF758079)

private val DashboardBackground = Color(0xFFF7F9F8)
private val DashboardWhite = Color(0xFFFFFFFF)
private val DashboardBorder = Color(0xFFE1E7E3)

@Composable
fun DashboardScreen(
    session: Session?,
    trips: List<Trip>,
    summary: DashboardSummary?,
    loading: Boolean,
    message: String?,
    onRefresh: () -> Unit,
    onOpenTrip: (Trip) -> Unit,
    onDismissMessage: () -> Unit
) {
    val role = session?.role.orEmpty()

    val title = when (role) {
        "DRIVER" -> "Good to go, ${session?.username.orEmpty()}"
        "DISPATCHER" -> "Operations at a glance"
        "FLEET_MANAGER" -> "Fleet command center"
        "ADMIN" -> "Workspace overview"
        else -> "Overview"
    }

    val subtitle = when (role) {
        "DRIVER" ->
            "Your assigned routes, status and live location in one place."

        "DISPATCHER" ->
            "Keep assignments moving and resolve operational bottlenecks."

        "FLEET_MANAGER" ->
            "Plan trips against your real drivers and available vehicles."

        "ADMIN" ->
            "A clean view of access, fleet and operational activity."

        else -> ""
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardBackground),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 18.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {
            HeroHeader(
                title = title,
                subtitle = subtitle,
                role = role,
                loading = loading,
                onRefresh = onRefresh
            )
        }

        if (role != "DRIVER" && summary != null) {
            item {
                SummaryGrid(
                    s = summary,
                    role = role
                )
            }
        }

        if (role == "DRIVER") {

            val active =
                trips.firstOrNull {
                    it.status == "IN_PROGRESS"
                }

            item {
                DriverFocus(
                    active = active,
                    trips = trips,
                    onOpenTrip = onOpenTrip
                )
            }

        } else {

            item {
                OperationsPulse(trips)
            }

            item {
                SectionTitle(
                    title = "Recent operations",
                    subtitle = "Tap a trip to inspect details"
                )
            }

            items(
                trips.take(6),
                key = { it.id }
            ) {
                TripRow(
                    trip = it,
                    onOpenTrip = onOpenTrip
                )
            }
        }

        if (trips.isEmpty()) {
            item {
                EmptyState(role)
            }
        }

        if (message != null) {
            item {
                MessageCard(
                    message = message,
                    dismiss = onDismissMessage
                )
            }
        }
    }
}

@Composable
private fun HeroHeader(
    title: String,
    subtitle: String,
    role: String,
    loading: Boolean,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = DashboardWhite,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 18.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(DashboardGreen)
                        )

                        Spacer(
                            modifier = Modifier.width(7.dp)
                        )

                        Text(
                            text = "FLOW",
                            color = DashboardGreenDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(
                            text = "•",
                            color = DashboardMuted,
                            fontSize = 11.sp
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(
                            text = role.replace('_', ' '),
                            color = DashboardMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = title,
                        color = DashboardInk,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 30.sp
                    )
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = DashboardGreenSoft
                ) {

                    IconButton(
                        onClick = onRefresh
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = DashboardGreenDark,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Text(
                text = subtitle,
                color = DashboardMuted,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (loading) {
                                Color(0xFFE0A400)
                            } else {
                                DashboardGreen
                            }
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
                    color = DashboardMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SummaryGrid(
    s: DashboardSummary,
    role: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Metric(
                label = "Active trips",
                value = s.activeTrips.toString(),
                accent = DashboardGreen,
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.weight(1f)
            )

            Metric(
                label = "Assigned",
                value = s.assignedTrips.toString(),
                accent = DashboardGreenDark,
                icon = Icons.Default.LocalShipping,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Metric(
                label = "Available vehicles",
                value = s.availableVehicles.toString(),
                accent = DashboardInkSoft,
                icon = Icons.Default.DirectionsCar,
                modifier = Modifier.weight(1f)
            )

            Metric(
                label = if (role == "ADMIN") {
                    "Active drivers"
                } else {
                    "Fleet moving"
                },
                value = if (role == "ADMIN") {
                    s.activeDrivers.toString()
                } else {
                    s.activeVehicles.toString()
                },
                accent = DashboardGreen,
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Metric(
    label: String,
    value: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = DashboardWhite,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            DashboardBorder
        )
    ) {

        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = label,
                    color = DashboardMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Surface(
                    color = accent.copy(alpha = 0.09f),
                    shape = RoundedCornerShape(9.dp)
                ) {

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier
                            .padding(7.dp)
                            .size(16.dp)
                    )
                }
            }

            Text(
                text = value,
                color = DashboardInk,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DriverFocus(
    active: Trip?,
    trips: List<Trip>,
    onOpenTrip: (Trip) -> Unit
) {
    if (active != null) {

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = DashboardInk
        ) {

            Column(
                modifier = Modifier.padding(19.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(DashboardGreen)
                    )

                    Spacer(
                        modifier = Modifier.width(7.dp)
                    )

                    Text(
                        text = "LIVE ROUTE",
                        color = Color(0xFF9DE7B9),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    )
                }

                Text(
                    text = "${active.origin} → ${active.destination}",
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 27.sp
                )

                Text(
                    text = "${active.code} · ${active.vehiclePlate}",
                    color = Color(0xFFBFCBC4),
                    fontSize = 12.sp
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Button(
                    onClick = {
                        onOpenTrip(active)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DashboardGreen,
                        contentColor = Color.White
                    )
                ) {

                    Text(
                        text = "Open active trip",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.width(5.dp)
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

    } else {

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = DashboardWhite,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                DashboardBorder
            )
        ) {

            Column(
                modifier = Modifier.padding(19.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {

                Text(
                    text = "READY FOR DISPATCH",
                    color = DashboardGreenDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.3.sp
                )

                Text(
                    text = "No active trip",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = DashboardInk
                )

                Text(
                    text = "Assigned work will appear here when dispatch creates it.",
                    color = DashboardMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }

    SectionTitle(
        title = "Assigned work",
        subtitle = "Your next routes"
    )

    trips
        .filter {
            it.status == "ASSIGNED"
        }
        .take(4)
        .forEach {
            TripRow(
                trip = it,
                onOpenTrip = onOpenTrip
            )
        }
}

@Composable
private fun OperationsPulse(
    trips: List<Trip>
) {
    val active =
        trips.count {
            it.status == "IN_PROGRESS"
        }

    val assigned =
        trips.count {
            it.status == "ASSIGNED"
        }

    val completed =
        trips.count {
            it.status == "COMPLETED"
        }

    val progress =
        if (trips.isEmpty()) {
            0f
        } else {
            completed.toFloat() / trips.size
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = DashboardWhite,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            DashboardBorder
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Operational pulse",
                    color = DashboardInk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = DashboardGreenDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = if (active == 0) {
                    "No trips are moving right now"
                } else {
                    "$active trips are moving right now"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DashboardInk
            )

            Text(
                text = "$assigned waiting to depart · $completed completed",
                color = DashboardMuted,
                fontSize = 12.sp
            )

            Spacer(
                modifier = Modifier.height(1.dp)
            )

            LinearProgressIndicator(
                progress = {
                    progress
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = DashboardGreen,
                trackColor = DashboardGreenSoft
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {

        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = DashboardInk
        )

        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = DashboardMuted
        )
    }
}

@Composable
private fun TripRow(
    trip: Trip,
    onOpenTrip: (Trip) -> Unit
) {
    Card(
        onClick = {
            onOpenTrip(trip)
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = DashboardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            DashboardBorder
        )
    ) {

        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                color = DashboardGreenSoft,
                shape = RoundedCornerShape(11.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = DashboardGreenDark,
                    modifier = Modifier
                        .padding(9.dp)
                        .size(19.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = trip.code,
                    color = DashboardGreenDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${trip.origin} → ${trip.destination}",
                    color = DashboardInk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${trip.driverUsername} · ${trip.vehiclePlate}",
                    color = DashboardMuted,
                    fontSize = 11.sp
                )
            }

            StatusPill(
                trip.status
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    dismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFF8E8),
        shape = RoundedCornerShape(15.dp)
    ) {

        Row(
            modifier = Modifier.padding(
                start = 12.dp,
                top = 10.dp,
                bottom = 10.dp,
                end = 6.dp
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = Color(0xFF7A5700),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            TextButton(
                onClick = dismiss
            ) {

                Text(
                    text = "Dismiss",
                    color = Color(0xFF7A5700),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}