package com.aldxynnn.flow.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val FlowBlue = Color(0xFF0B7A55) // legacy name kept for source compatibility
val FlowBlueDark = Color(0xFF07583E)
val FlowInk = Color(0xFF112F25)
val FlowBackground = Color(0xFFF4F7F5)
val FlowSurface = Color(0xFFFFFFFF)
val FlowGreen = Color(0xFF0B7A55)
val FlowRed = Color(0xFFB42318)
val FlowAmber = Color(0xFF9A6700)
val Muted = Color(0xFF66756E)
val Border = Color(0xFFD9E3DE)
val SoftBlue = Color(0xFFE3F2EC) // legacy name kept for compatibility
val SoftGreen = Color(0xFFE3F2EC)
val SoftAmber = Color(0xFFFFF4DB)
val SoftRed = Color(0xFFFEEFEB)

val FlowTypography = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.35).sp
        ),
        headlineMedium = headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.2).sp
        ),
        titleLarge = titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 19.sp
        ),
        titleMedium = titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        ),
        bodyLarge = bodyLarge.copy(
            fontSize = 15.sp,
            lineHeight = 21.sp
        ),
        bodyMedium = bodyMedium.copy(
            fontSize = 13.sp,
            lineHeight = 19.sp
        ),
        labelLarge = labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    )
}

@Composable
fun FlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = FlowGreen,
            onPrimary = Color.White,
            primaryContainer = SoftGreen,
            onPrimaryContainer = FlowBlueDark,
            secondary = FlowInk,
            background = FlowBackground,
            surface = FlowSurface,
            surfaceVariant = Color(0xFFEEF2EF),
            onSurface = FlowInk,
            onBackground = FlowInk,
            outline = Border,
            outlineVariant = Border.copy(alpha = 0.75f),
            error = FlowRed,
            onError = Color.White
        ),
        typography = FlowTypography,
        content = content
    )
}
