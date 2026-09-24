package com.aldxynnn.flow.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldxynnn.flow.core.network.Trip
import com.aldxynnn.flow.core.network.TripLocation

private val TripGreen =
    Color(0xFF00B14F)

private val TripGreenDark =
    Color(0xFF008F3F)

private val TripGreenSoft =
    Color(0xFFE8F7EE)

private val TripInk =
    Color(0xFF17211B)

private val TripInkSoft =
    Color(0xFF3C4741)

private val TripMuted =
    Color(0xFF758079)

private val TripBackground =
    Color(0xFFF7F9F8)

private val TripWhite =
    Color(0xFFFFFFFF)

private val TripBorder =
    Color(0xFFE1E7E3)

private val TripWarning =
    Color(0xFFB97816)

private val TripWarningSoft =
    Color(0xFFFFF5E3)

private val TripDanger =
    Color(0xFFB33B34)

private val TripDangerSoft =
    Color(0xFFFCEDEC)

@Composable
fun TripDetailScreen(
    trip: Trip,
    role: String,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onAssign: (String) -> Unit,
    locations: List<TripLocation>,
    actionLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                TripBackground
            )
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                start = 18.dp,
                end = 18.dp,
                top = 18.dp,
                bottom = 24.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBack,
                contentPadding =
                    PaddingValues(
                        horizontal = 4.dp,
                        vertical = 8.dp
                    )
            ) {
                Text(
                    text = "← Back",
                    color = TripInkSoft,
                    fontSize = 13.sp,
                    fontWeight =
                        FontWeight.SemiBold
                )
            }

            Spacer(
                modifier =
                    Modifier.weight(1f)
            )

            TripStatusPill(
                status = trip.status
            )
        }

        Surface(
            modifier =
                Modifier.fillMaxWidth(),
            color = TripWhite,
            shape =
                RoundedCornerShape(
                    22.dp
                ),
            border =
                BorderStroke(
                    1.dp,
                    TripBorder
                )
        ) {

            Column(
                modifier =
                    Modifier.padding(
                        19.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(7.dp)
            ) {

                Text(
                    text = trip.code,
                    color = TripGreenDark,
                    fontSize = 11.sp,
                    fontWeight =
                        FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text =
                        "${trip.origin} → ${trip.destination}",
                    color = TripInk,
                    fontSize = 25.sp,
                    lineHeight = 30.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "${trip.vehiclePlate}  •  ${trip.driverUsername}",
                    color = TripMuted,
                    fontSize = 13.sp,
                    fontWeight =
                        FontWeight.Medium
                )
            }
        }

        TripMapCard(
            trip = trip,
            locations = locations
        )

        Surface(
            modifier =
                Modifier.fillMaxWidth(),
            color = TripWhite,
            shape =
                RoundedCornerShape(
                    20.dp
                ),
            border =
                BorderStroke(
                    1.dp,
                    TripBorder
                )
        ) {

            Column(
                modifier =
                    Modifier.padding(
                        18.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        0.dp
                    )
            ) {

                Text(
                    text = "TRIP STATUS",
                    color = TripMuted,
                    fontSize = 10.sp,
                    fontWeight =
                        FontWeight.Bold,
                    letterSpacing =
                        1.2.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                AnimatedContent(
                    targetState =
                        trip.status,
                    transitionSpec = {
                        (
                                fadeIn() +
                                        scaleIn()
                                ).togetherWith(
                                fadeOut()
                            )
                    },
                    label =
                        "trip_status"
                ) { state ->

                    Text(
                        text =
                            state.replace(
                                '_',
                                ' '
                            ),
                        color =
                            statusColor(
                                state
                            ),
                        fontSize = 24.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                HorizontalDivider(
                    color =
                        TripBorder
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

                InfoLine(
                    label = "Origin",
                    value = trip.origin
                )

                InfoDivider()

                InfoLine(
                    label = "Destination",
                    value = trip.destination
                )

                InfoDivider()

                InfoLine(
                    label = "Vehicle",
                    value =
                        trip.vehiclePlate
                )

                InfoDivider()

                InfoLine(
                    label = "Driver",
                    value =
                        trip.driverUsername
                )

                InfoDivider()

                InfoLine(
                    label = "ETA",
                    value =
                        "${trip.etaMinutes} minutes"
                )

                InfoDivider()

                InfoLine(
                    label = "Last GPS",
                    value =
                        trip.latitude?.let { lat ->

                            "%.5f, %.5f".format(
                                lat,
                                trip.longitude
                                    ?: 0.0
                            )

                        } ?: "Waiting for location"
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    2.dp
                )
        )

        if (role == "DRIVER") {

            when (trip.status) {

                "ASSIGNED" -> {

                    Button(
                        onClick = onStart,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    54.dp
                                ),
                        shape =
                            RoundedCornerShape(
                                14.dp
                            ),
                        enabled =
                            !actionLoading,
                        colors =
                            ButtonDefaults
                                .buttonColors(
                                    containerColor =
                                        TripGreen,
                                    contentColor =
                                        Color.White,
                                    disabledContainerColor =
                                        TripGreen.copy(
                                            alpha =
                                                0.55f
                                        ),
                                    disabledContentColor =
                                        Color.White
                                )
                    ) {

                        Text(
                            text =
                                if (
                                    actionLoading
                                ) {
                                    "Starting…"
                                } else {
                                    "Start trip"
                                },
                            fontSize = 14.sp,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }

                "IN_PROGRESS" -> {

                    Button(
                        onClick =
                            onComplete,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    54.dp
                                ),
                        shape =
                            RoundedCornerShape(
                                14.dp
                            ),
                        enabled =
                            !actionLoading,
                        colors =
                            ButtonDefaults
                                .buttonColors(
                                    containerColor =
                                        TripGreen,
                                    contentColor =
                                        Color.White,
                                    disabledContainerColor =
                                        TripGreen.copy(
                                            alpha =
                                                0.55f
                                        ),
                                    disabledContentColor =
                                        Color.White
                                )
                    ) {

                        Text(
                            text =
                                if (
                                    actionLoading
                                ) {
                                    "Completing…"
                                } else {
                                    "Complete trip"
                                },
                            fontSize = 14.sp,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }

                else -> {

                    Surface(
                        modifier =
                            Modifier.fillMaxWidth(),
                        color = TripWhite,
                        shape =
                            RoundedCornerShape(
                                14.dp
                            ),
                        border =
                            BorderStroke(
                                1.dp,
                                TripBorder
                            )
                    ) {

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        17.dp
                                    ),
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text =
                                    "Trip ${
                                        trip.status
                                            .lowercase()
                                            .replace(
                                                '_',
                                                ' '
                                            )
                                    }",
                                color =
                                    TripMuted,
                                fontSize = 13.sp,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }
                }
            }

        } else {

            Surface(
                modifier =
                    Modifier.fillMaxWidth(),
                color = TripWhite,
                shape =
                    RoundedCornerShape(
                        18.dp
                    ),
                border =
                    BorderStroke(
                        1.dp,
                        TripBorder
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(
                            16.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            7.dp
                        )
                ) {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Box(
                            modifier =
                                Modifier
                                    .size(
                                        7.dp
                                    )
                                    .background(
                                        TripGreen,
                                        RoundedCornerShape(
                                            50
                                        )
                                    )
                        )

                        Spacer(
                            modifier =
                                Modifier.width(
                                    7.dp
                                )
                        )

                        Text(
                            text =
                                "OPERATIONS VIEW",
                            color =
                                TripGreenDark,
                            fontSize = 10.sp,
                            fontWeight =
                                FontWeight.Bold,
                            letterSpacing =
                                1.sp
                        )
                    }

                    Text(
                        text =
                            "Driver: ${trip.driverUsername}",
                        color = TripInk,
                        fontSize = 14.sp,
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Text(
                        text =
                            "Use the assignment board to change ownership of this trip.",
                        color = TripMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TripStatusPill(
    status: String
) {
    val color =
        statusColor(status)

    Surface(
        color =
            when (status) {
                "IN_PROGRESS" ->
                    TripGreenSoft

                "COMPLETED" ->
                    Color(0xFFF0F2F1)

                "CANCELLED" ->
                    TripDangerSoft

                else ->
                    TripWarningSoft
            },
        shape =
            RoundedCornerShape(
                50
            )
    ) {

        Text(
            text =
                status.replace(
                    '_',
                    ' '
                ),
            modifier =
                Modifier.padding(
                    horizontal = 11.dp,
                    vertical = 7.dp
                ),
            color = color,
            fontSize = 10.sp,
            fontWeight =
                FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun InfoLine(
    label: String,
    value: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 10.dp
                ),
        verticalAlignment =
            Alignment.Top
    ) {

        Text(
            text = label,
            modifier =
                Modifier.width(82.dp),
            color = TripMuted,
            fontSize = 12.sp,
            fontWeight =
                FontWeight.Medium
        )

        Spacer(
            modifier =
                Modifier.width(12.dp)
        )

        Text(
            text = value,
            modifier =
                Modifier.weight(1f),
            color = TripInk,
            fontSize = 13.sp,
            fontWeight =
                FontWeight.SemiBold
        )
    }
}

@Composable
private fun InfoDivider() {
    HorizontalDivider(
        color =
            TripBorder
    )
}

private fun statusColor(
    status: String
): Color =
    when (status) {

        "IN_PROGRESS" ->
            TripGreen

        "COMPLETED" ->
            TripMuted

        "CANCELLED" ->
            TripDanger

        else ->
            TripWarning
    }