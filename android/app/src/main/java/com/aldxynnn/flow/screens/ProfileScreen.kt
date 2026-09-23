package com.aldxynnn.flow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldxynnn.flow.core.session.Session

private val ProfileGreen = Color(0xFF00B14F)
private val ProfileGreenDark = Color(0xFF008F3F)
private val ProfileGreenSoft = Color(0xFFE8F7EE)

private val ProfileInk = Color(0xFF17211B)
private val ProfileInkSoft = Color(0xFF3C4741)
private val ProfileMuted = Color(0xFF758079)

private val ProfileBackground = Color(0xFFF7F9F8)
private val ProfileWhite = Color(0xFFFFFFFF)
private val ProfileBorder = Color(0xFFE1E7E3)

@Composable
fun ProfileScreen(
    session: Session?,
    onLogout: () -> Unit
) {
    val role = session?.role.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
            .padding(
                start = 18.dp,
                end = 18.dp,
                top = 18.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        ProfileHeader(
            username = session?.username ?: "User",
            role = role
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ProfileWhite,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                ProfileBorder
            )
        ) {

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {

                Text(
                    text = "Account information",
                    color = ProfileInk,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Info(
                    label = "Access",
                    value = roleDescription(role)
                )

                ProfileDivider()

                Info(
                    label = "Authentication",
                    value = "JWT session"
                )

                ProfileDivider()

                Info(
                    label = "Driver sync",
                    value = if (role == "DRIVER") {
                        "Offline queue + background retry"
                    } else {
                        "Server-backed live data"
                    }
                )

                ProfileDivider()

                Info(
                    label = "Data source",
                    value = "Operational database"
                )
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ProfileWhite,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                ProfileBorder
            )
        ) {

            Column(
                modifier = Modifier.padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    text = "Session",
                    color = ProfileMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "You are signed in as ${session?.username ?: "User"}",
                    color = ProfileInkSoft,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        ProfileBorder
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ProfileInk
                    )
                ) {
                    Text(
                        text = "Sign out",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    username: String,
    role: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ProfileWhite,
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            ProfileBorder
        )
    ) {

        Row(
            modifier = Modifier.padding(19.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(56.dp),
                color = ProfileGreenSoft,
                shape = RoundedCornerShape(16.dp)
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = username
                            .take(1)
                            .uppercase(),
                        color = ProfileGreenDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )

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
                                ProfileGreen,
                                RoundedCornerShape(50)
                            )
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text(
                        text = "ACCOUNT",
                        color = ProfileGreenDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                Text(
                    text = username,
                    color = ProfileInk,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = role.replace('_', ' '),
                    color = ProfileMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun Info(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = label,
            color = ProfileMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = value,
            color = ProfileInk,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(
            vertical = 13.dp
        ),
        color = ProfileBorder
    )
}

private fun roleDescription(role: String) =
    when (role) {
        "ADMIN" ->
            "Users and system access"

        "FLEET_MANAGER" ->
            "Fleet planning and trip creation"

        "DISPATCHER" ->
            "Live assignment and operations"

        "DRIVER" ->
            "Assigned routes and GPS execution"

        else ->
            "Standard access"
    }