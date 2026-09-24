package com.aldxynnn.flow.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FlowBg = Color(0xFFF7F9F8)
private val FlowWhite = Color(0xFFFFFFFF)

private val FlowInk = Color(0xFF17211B)
private val FlowInkSoft = Color(0xFF344039)
private val FlowMuted = Color(0xFF6F7A73)

private val FlowGreen = Color(0xFF00B14F)
private val FlowGreenDark = Color(0xFF008F3F)
private val FlowGreenSoft = Color(0xFFE1F7E9)

private val FlowBorder = Color(0xFFDCE5DF)
private val FlowError = Color(0xFFD92D48)

@Composable
fun LoginScreen(
    loading: Boolean,
    message: String?,
    onLogin: (String, String) -> Unit,
    onDismissMessage: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val transition = rememberInfiniteTransition(
        label = "flow_login"
    )

    val routeProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 5200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "route"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FlowBg)
    ) {

        FleetHero(
            progress = routeProgress,
            pulse = pulse
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    start = 18.dp,
                    end = 18.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.weight(1f)
            )

            LoginPanel(
                username = username,
                password = password,
                passwordVisible = passwordVisible,
                loading = loading,
                message = message,
                onUsernameChange = {
                    username = it
                },
                onPasswordChange = {
                    password = it
                },
                onTogglePassword = {
                    passwordVisible = !passwordVisible
                },
                onLogin = {
                    onLogin(
                        username.trim(),
                        password
                    )
                },
                onDismissMessage = onDismissMessage
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 4.dp,
                        end = 4.dp,
                        bottom = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(
                            FlowBorder.copy(alpha = 0.8f)
                        )
                )

                Text(
                    text = "FLOW",
                    modifier = Modifier.padding(
                        horizontal = 10.dp
                    ),
                    color = FlowMuted.copy(alpha = 0.55f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(
                            FlowBorder.copy(alpha = 0.8f)
                        )
                )
            }
        }
    }
}

@Composable
private fun LoginPanel(
    username: String,
    password: String,
    passwordVisible: Boolean,
    loading: Boolean,
    message: String?,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onLogin: () -> Unit,
    onDismissMessage: () -> Unit
) {
    val canLogin =
        username.isNotBlank() &&
                password.isNotBlank() &&
                !loading

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(26.dp)
            )
            .background(FlowWhite)
            .border(
                width = 1.dp,
                color = FlowBorder,
                shape = RoundedCornerShape(26.dp)
            )
            .padding(
                horizontal = 20.dp,
                vertical = 27.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Welcome back",
                    color = FlowInk,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Sign in to your operations workspace",
                    color = FlowMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(19.dp)
        )

        FlowInput(
            value = username,
            onValueChange = onUsernameChange,
            placeholder = "Username",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.PersonOutline,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        Spacer(
            modifier = Modifier.height(11.dp)
        )

        FlowInput(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Password",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = onTogglePassword
                ) {
                    Icon(
                        imageVector =
                            if (passwordVisible) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )
                }
            },
            visualTransformation =
                if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        Button(
            onClick = onLogin,
            enabled = canLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FlowGreen,
                contentColor = Color.White,
                disabledContainerColor =
                    FlowGreen.copy(alpha = 0.18f),
                disabledContentColor =
                    Color.White.copy(alpha = 0.7f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 1.dp
            )
        ) {

            if (loading) {

                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "Connecting...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

            } else {

                Text(
                    text = "Enter FLOW",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {

            message?.let { error ->

                Spacer(
                    modifier = Modifier.height(9.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(11.dp)
                        )
                        .background(
                            FlowError.copy(alpha = 0.07f)
                        )
                        .clickable {
                            onDismissMessage()
                        }
                        .padding(
                            horizontal = 11.dp,
                            vertical = 9.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(FlowError)
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = error,
                        color = FlowError,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FleetHero(
    progress: Float,
    pulse: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
    ) {

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            val w = size.width
            val h = size.height

            drawCircle(
                color = FlowGreen.copy(alpha = 0.055f),
                radius = 185.dp.toPx(),
                center = Offset(
                    w * 0.78f,
                    h * 0.18f
                )
            )

            drawCircle(
                color = FlowGreen.copy(alpha = 0.035f),
                radius = 125.dp.toPx(),
                center = Offset(
                    w * 0.15f,
                    h * 0.72f
                )
            )

            val route = Path().apply {

                moveTo(
                    w * 0.05f,
                    h * 0.80f
                )

                cubicTo(
                    w * 0.18f,
                    h * 0.50f,
                    w * 0.28f,
                    h * 0.88f,
                    w * 0.43f,
                    h * 0.68f
                )

                cubicTo(
                    w * 0.57f,
                    h * 0.46f,
                    w * 0.62f,
                    h * 0.82f,
                    w * 0.77f,
                    h * 0.59f
                )

                cubicTo(
                    w * 0.85f,
                    h * 0.42f,
                    w * 0.91f,
                    h * 0.47f,
                    w * 0.96f,
                    h * 0.34f
                )
            }

            drawPath(
                path = route,
                color = FlowGreen.copy(alpha = 0.10f),
                style = Stroke(
                    width = 13.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            drawPath(
                path = route,
                color = FlowGreen.copy(alpha = 0.30f),
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            drawPath(
                path = route,
                color = FlowGreen.copy(alpha = 0.85f),
                style = Stroke(
                    width = 1.2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(
                            8.dp.toPx(),
                            10.dp.toPx()
                        ),
                        phase = -progress * 80.dp.toPx()
                    )
                )
            )

            val measure = PathMeasure()

            measure.setPath(
                route,
                false
            )

            val point = measure.getPosition(
                measure.length * progress
            )

            drawCircle(
                color = FlowGreen.copy(alpha = 0.09f),
                radius = 28.dp.toPx() * pulse,
                center = point
            )

            drawCircle(
                color = FlowGreen.copy(alpha = 0.18f),
                radius = 13.dp.toPx(),
                center = point
            )

            drawCircle(
                color = FlowWhite,
                radius = 7.dp.toPx(),
                center = point
            )

            drawCircle(
                color = FlowGreen,
                radius = 4.dp.toPx(),
                center = point
            )

            val nodes = listOf(
                Offset(
                    w * 0.05f,
                    h * 0.80f
                ),
                Offset(
                    w * 0.43f,
                    h * 0.68f
                ),
                Offset(
                    w * 0.77f,
                    h * 0.59f
                ),
                Offset(
                    w * 0.96f,
                    h * 0.34f
                )
            )

            nodes.forEachIndexed { index, node ->

                drawCircle(
                    color = FlowWhite,
                    radius = 6.dp.toPx(),
                    center = node
                )

                drawCircle(
                    color =
                        if (index == 0 || index == 3) {
                            FlowGreen
                        } else {
                            FlowGreen.copy(alpha = 0.45f)
                        },
                    radius = 3.dp.toPx(),
                    center = node
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(
                    start = 24.dp,
                    top = 54.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            FlowMark()

            Spacer(
                modifier = Modifier.width(11.dp)
            )

            Column {

                Text(
                    text = "FLOW",
                    color = FlowInk,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp
                )

                Text(
                    text = "OPERATIONS",
                    color = FlowMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.1.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = 56.dp,
                    end = 22.dp
                )
                .clip(
                    RoundedCornerShape(50.dp)
                )
                .background(
                    FlowWhite.copy(alpha = 0.92f)
                )
                .border(
                    1.dp,
                    FlowBorder,
                    RoundedCornerShape(50.dp)
                )
                .padding(
                    horizontal = 11.dp,
                    vertical = 7.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(FlowGreen)
            )

            Spacer(
                modifier = Modifier.width(6.dp)
            )

            Text(
                text = "SYSTEM ONLINE",
                color = FlowInkSoft,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 180.dp)
                .clip(
                    RoundedCornerShape(13.dp)
                )
                .background(
                    FlowWhite.copy(alpha = 0.94f)
                )
                .border(
                    1.dp,
                    FlowBorder,
                    RoundedCornerShape(13.dp)
                )
                .padding(
                    horizontal = 12.dp,
                    vertical = 9.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            FlowMiniMark()

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Column {

                Text(
                    text = "ACTIVE ROUTE",
                    color = FlowMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "TRP-2048",
                    color = FlowInk,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        RouteLabel(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 20.dp,
                    bottom = 48.dp
                ),
            title = "DEPOT",
            subtitle = "Jakarta"
        )

        RouteLabel(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 20.dp,
                    bottom = 78.dp
                ),
            title = "DESTINATION",
            subtitle = "Bekasi"
        )
    }
}

@Composable
private fun FlowMark() {

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(
                RoundedCornerShape(13.dp)
            )
            .background(FlowInk)
            .padding(9.dp)
    ) {

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            val w = size.width
            val h = size.height

            val first = Path().apply {

                moveTo(
                    w * 0.08f,
                    h * 0.28f
                )

                cubicTo(
                    w * 0.34f,
                    h * 0.05f,
                    w * 0.64f,
                    h * 0.05f,
                    w * 0.92f,
                    h * 0.28f
                )
            }

            val second = Path().apply {

                moveTo(
                    w * 0.08f,
                    h * 0.72f
                )

                cubicTo(
                    w * 0.34f,
                    h * 0.95f,
                    w * 0.64f,
                    h * 0.95f,
                    w * 0.92f,
                    h * 0.72f
                )
            }

            drawPath(
                path = first,
                color = FlowGreen,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            drawPath(
                path = second,
                color = Color.White,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            drawCircle(
                color = FlowGreen,
                radius = 2.3.dp.toPx(),
                center = Offset(
                    w * 0.92f,
                    h * 0.28f
                )
            )
        }
    }
}

@Composable
private fun FlowMiniMark() {

    Box(
        modifier = Modifier
            .size(25.dp)
            .clip(
                RoundedCornerShape(8.dp)
            )
            .background(FlowGreenSoft),
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier.size(15.dp)
        ) {

            drawArc(
                color = FlowGreen,
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            drawArc(
                color = FlowGreenDark,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Composable
private fun FlowStatusIndicator() {

    Row(
        modifier = Modifier
            .clip(
                RoundedCornerShape(50.dp)
            )
            .background(
                FlowGreen.copy(alpha = 0.08f)
            )
            .padding(
                horizontal = 9.dp,
                vertical = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(FlowGreen)
        )

        Spacer(
            modifier = Modifier.width(5.dp)
        )

        Text(
            text = "READY",
            color = FlowGreen,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun RouteLabel(
    modifier: Modifier,
    title: String,
    subtitle: String
) {

    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(11.dp)
            )
            .background(
                FlowWhite.copy(alpha = 0.9f)
            )
            .border(
                1.dp,
                FlowBorder,
                RoundedCornerShape(11.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 7.dp
            )
    ) {

        Text(
            text = title,
            color = FlowMuted,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp
        )

        Text(
            text = subtitle,
            color = FlowInk,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun FlowInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation =
        VisualTransformation.None,
    keyboardOptions: KeyboardOptions =
        KeyboardOptions.Default
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,

        placeholder = {
            Text(
                text = placeholder,
                color = FlowMuted.copy(alpha = 0.75f),
                fontSize = 14.sp
            )
        },

        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,

        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,

        shape = RoundedCornerShape(14.dp),

        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FlowBg,
            unfocusedContainerColor = FlowBg,
            focusedBorderColor = FlowGreen,
            unfocusedBorderColor = FlowBorder,
            focusedTextColor = FlowInk,
            unfocusedTextColor = FlowInk,
            focusedLeadingIconColor = FlowGreenDark,
            unfocusedLeadingIconColor = FlowMuted,
            focusedTrailingIconColor = FlowGreenDark,
            unfocusedTrailingIconColor = FlowMuted,
            cursorColor = FlowGreen
        )
    )
}