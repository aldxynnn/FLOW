package com.aldxynnn.flow.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val FlowBlue = Color(0xFF246BFD)
val FlowBlueDark = Color(0xFF1749B7)
val FlowInk = Color(0xFF101828)
val FlowBackground = Color(0xFFF6F8FB)
val FlowSurface = Color(0xFFFFFFFF)
val FlowGreen = Color(0xFF16A05D)
val FlowRed = Color(0xFFD64545)
val FlowAmber = Color(0xFFE29A17)
val Muted = Color(0xFF667085)
val Border = Color(0xFFE4E7EC)
val SoftBlue = Color(0xFFEAF1FF)
val SoftGreen = Color(0xFFEAF8F1)
val SoftAmber = Color(0xFFFFF5DF)
val SoftRed = Color(0xFFFFECEC)

val FlowTypography = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Black, fontSize = 32.sp, lineHeight = 38.sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Black, fontSize = 26.sp),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold),
        bodyLarge = bodyLarge.copy(fontSize = 15.sp),
        bodyMedium = bodyMedium.copy(fontSize = 13.sp)
    )
}

@Composable
fun FlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = FlowBlue,
            onPrimary = Color.White,
            primaryContainer = SoftBlue,
            onPrimaryContainer = FlowBlueDark,
            secondary = FlowInk,
            background = FlowBackground,
            surface = FlowSurface,
            surfaceVariant = Color(0xFFF0F2F5),
            onSurface = FlowInk,
            onBackground = FlowInk,
            outline = Border,
            error = FlowRed
        ),
        typography = FlowTypography,
        content = content
    )
}
