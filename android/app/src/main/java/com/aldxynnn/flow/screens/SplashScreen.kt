package com.aldxynnn.flow.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldxynnn.flow.R
import kotlin.math.cos
import kotlin.math.sin

/*
 * ============================================================
 * FLOW SPLASH COLORS
 * ============================================================
 *
 * IMPORTANT:
 * The top background matches the Android system splash
 * background so the transition does not flash to another color.
 */
private val FlowBackgroundTop = Color(0xFFF4F8F6)
private val FlowBackgroundBottom = Color(0xFFEFF6F2)

private val FlowInk = Color(0xFF12352A)
private val FlowMuted = Color(0xFF70827A)

private val FlowGreen = Color(0xFF087A55)
private val FlowGreenDark = Color(0xFF075B42)
private val FlowGreenLight = Color(0xFF19A875)

private val FlowRoad = Color(0xFFABC6BB)
private val FlowSoft = Color(0xFFDCEBE5)


@Composable
fun FlowSplashScreen(
    onFinished: () -> Unit
) {
    /*
     * ============================================================
     * MASTER TIMELINE
     * ============================================================
     *
     * 0.00 -> 0.20  logo handoff: center -> final position
     * 0.07 -> 0.32  branding text entrance
     * 0.20 -> 1.00  truck journey
     *
     * The Android 12+ system splash uses the exact same
     * R.drawable.flow_logo and starts in the center.
     *
     * The Compose splash therefore starts with the same logo
     * in the same visual position, then moves it into the
     * final branding position. This removes the visual feeling
     * of two separate splash screens.
     */
    val progress = remember {
        Animatable(0f)
    }

    LaunchedEffect(Unit) {

        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 3600,
                easing = FastOutSlowInEasing
            )
        )

        /*
         * Navigation happens immediately when progress reaches 1f.
         */
        onFinished()
    }

    val animation = progress.value

    /*
     * ============================================================
     * BRAND TIMING
     * ============================================================
     */

    val logoFlight =
        (animation / 0.20f)
            .coerceIn(0f, 1f)

    val brandProgress =
        ((animation - 0.07f) / 0.25f)
            .coerceIn(0f, 1f)

    /*
     * ============================================================
     * TRUCK TIMING
     * ============================================================
     */

    val truckProgress =
        ((animation - 0.20f) / 0.80f)
            .coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        FlowBackgroundTop,
                        FlowBackgroundBottom
                    )
                )
            )
    ) {

        /*
         * ========================================================
         * ATMOSPHERE
         * ========================================================
         */

        SplashAtmosphere(
            modifier = Modifier.fillMaxSize(),
            progress = animation
        )

        /*
         * ========================================================
         * MAIN FLEET ANIMATION
         * ========================================================
         */

        SplashFleetScene(
            progress = truckProgress,
            modifier = Modifier.fillMaxSize()
        )

        /*
         * ========================================================
         * BRANDING LAYOUT
         * ========================================================
         *
         * This column intentionally keeps the original layout
         * proportions of the splash that were already working.
         *
         * The actual logo is overlaid separately so it can travel
         * from the system-splash center into this exact logo slot.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.weight(0.72f)
            )

            /*
             * Reserve the original logo space.
             */
            Spacer(
                modifier = Modifier.size(116.dp)
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            /*
             * ====================================================
             * BRAND TEXT
             * ====================================================
             */

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(brandProgress)
            ) {

                Text(
                    text = "FLOW",
                    color = FlowInk,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.2.sp
                )

                Spacer(
                    modifier = Modifier.height(7.dp)
                )

                Text(
                    text = "FLEET & OPERATIONS",
                    color = FlowMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.8.sp
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Text(
                    text = "MOVE SMART. OPERATE BETTER.",
                    color = FlowGreenDark.copy(
                        alpha = 0.70f
                    ),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.9.sp
                )
            }

            Spacer(
                modifier = Modifier.weight(1.35f)
            )

            /*
             * Tiny brand indicator.
             */
            Box(
                modifier = Modifier
                    .padding(bottom = 28.dp)
                    .size(5.dp)
                    .background(
                        FlowGreen.copy(alpha = 0.28f)
                    )
            )
        }

        /*
         * ========================================================
         * CONTINUOUS SYSTEM-SPLASH -> COMPOSE LOGO HANDOFF
         * ========================================================
         *
         * At progress = 0:
         *
         *      logo center = screen center
         *      logo size   = 96dp
         *
         * The system splash also shows the same asset in the
         * center. Compose therefore takes over on the same frame.
         *
         * By progress = 0.20:
         *
         *      logo center = original FLOW branding position
         *      logo size   = 116dp
         *
         * No second logo entrance occurs.
         */
        val finalLogoOffsetY =
            -(maxHeight * 0.17f)

        val logoOffsetY =
            finalLogoOffsetY * logoFlight

        val logoSize =
            lerp(
                96.dp,
                116.dp,
                logoFlight
            )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.flow_logo
                ),
                contentDescription = "FLOW",
                modifier = Modifier
                    .size(logoSize)
                    .offset(y = logoOffsetY),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/*
 * ============================================================
 * ATMOSPHERE
 * ============================================================
 */

@Composable
private fun SplashAtmosphere(
    modifier: Modifier,
    progress: Float
) {
    Canvas(
        modifier = modifier
    ) {

        val width = size.width
        val height = size.height

        /*
         * ========================================================
         * BRANDING GLOW
         * ========================================================
         */

        val center =
            Offset(
                width * 0.50f,
                height * 0.38f
            )

        drawCircle(
            color = FlowGreen.copy(alpha = 0.020f),
            radius = width * 0.27f,
            center = center
        )

        drawCircle(
            color = FlowGreen.copy(alpha = 0.011f),
            radius = width * 0.43f,
            center = center
        )

        /*
         * ========================================================
         * OPERATIONAL HORIZON
         * ========================================================
         */

        drawLine(
            color = FlowGreen.copy(alpha = 0.025f),
            start = Offset(
                width * 0.08f,
                height * 0.61f
            ),
            end = Offset(
                width * 0.92f,
                height * 0.61f
            ),
            strokeWidth = 1.dp.toPx()
        )

        drawLine(
            color = FlowGreen.copy(alpha = 0.020f),
            start = Offset(
                width * 0.18f,
                height * 0.87f
            ),
            end = Offset(
                width * 0.82f,
                height * 0.87f
            ),
            strokeWidth = 1.dp.toPx()
        )

        /*
         * ========================================================
         * SUBTLE MOVING SIGNAL
         * ========================================================
         */

        if (progress > 0.18f) {

            val signal =
                ((progress - 0.18f) / 0.82f)
                    .coerceIn(0f, 1f)

            val x =
                width * (
                        0.12f +
                                (0.76f * signal)
                        )

            drawCircle(
                color = FlowGreen.copy(alpha = 0.045f),
                radius = 3.dp.toPx(),
                center = Offset(
                    x,
                    height * 0.61f
                )
            )
        }
    }
}


/*
 * ============================================================
 * FLEET SCENE
 * ============================================================
 */

@Composable
private fun SplashFleetScene(
    progress: Float,
    modifier: Modifier
) {
    Canvas(
        modifier = modifier
    ) {

        val width = size.width
        val height = size.height

        val safeProgress =
            progress.coerceIn(0f, 1f)

        /*
         * ========================================================
         * ROAD
         * ========================================================
         */

        val roadY =
            height * 0.735f

        /*
         * ========================================================
         * TRUCK SIZE
         * ========================================================
         */

        val truckWidth =
            (width * 0.255f)
                .coerceIn(
                    116.dp.toPx(),
                    176.dp.toPx()
                )

        val truckHeight =
            truckWidth * 0.43f

        /*
         * ========================================================
         * DESTINATION
         * ========================================================
         */

        val destinationX =
            width * 0.86f

        /*
         * ========================================================
         * TRUCK POSITION
         * ========================================================
         */

        val startX =
            -truckWidth * 1.35f

        val endX =
            destinationX -
                    (truckWidth * 0.96f)

        val truckX =
            startX +
                    (
                            (endX - startX) *
                                    safeProgress
                            )

        /*
         * ========================================================
         * VERTICAL SETTLING
         * ========================================================
         */

        val settle =
            if (safeProgress > 0.82f) {

                ((safeProgress - 0.82f) / 0.18f)
                    .coerceIn(0f, 1f)

            } else {

                0f
            }

        val truckY =
            sin(
                settle.toDouble() * Math.PI
            ).toFloat() * 1.8f

        val truckBottom =
            roadY -
                    5.dp.toPx() -
                    truckY

        val truckTop =
            truckBottom -
                    truckHeight

        /*
         * ========================================================
         * DESTINATION
         * ========================================================
         */

        val destinationCenter =
            Offset(
                destinationX,
                roadY - 18.dp.toPx()
            )

        /*
         * ========================================================
         * ARRIVAL
         * ========================================================
         */

        val arrival =
            if (safeProgress > 0.72f) {

                ((safeProgress - 0.72f) / 0.28f)
                    .coerceIn(0f, 1f)

            } else {

                0f
            }

        /*
         * ========================================================
         * ARRIVAL RINGS
         * ========================================================
         */

        if (arrival > 0f) {

            val pulse =
                sin(
                    arrival.toDouble() * Math.PI
                ).toFloat()

            drawCircle(
                color = FlowGreen.copy(
                    alpha = 0.035f * pulse
                ),
                radius =
                    18.dp.toPx() +
                            (
                                    25.dp.toPx() *
                                            arrival
                                    ),
                center = destinationCenter
            )

            drawCircle(
                color = FlowGreen.copy(
                    alpha = 0.08f * pulse
                ),
                radius =
                    9.dp.toPx() +
                            (
                                    8.dp.toPx() *
                                            arrival
                                    ),
                center = destinationCenter,
                style = Stroke(
                    width = 1.2.dp.toPx()
                )
            )
        }

        /*
         * ========================================================
         * ROAD SHADOW
         * ========================================================
         */

        drawLine(
            color = FlowRoad.copy(alpha = 0.11f),
            start = Offset(
                width * 0.055f,
                roadY + 8.dp.toPx()
            ),
            end = Offset(
                width * 0.945f,
                roadY + 8.dp.toPx()
            ),
            strokeWidth = 8.dp.toPx(),
            cap = StrokeCap.Round
        )

        /*
         * Main road.
         */
        drawLine(
            color = FlowRoad.copy(alpha = 0.40f),
            start = Offset(
                width * 0.07f,
                roadY
            ),
            end = Offset(
                width * 0.93f,
                roadY
            ),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        /*
         * Lower reflection.
         */
        drawLine(
            color = FlowGreen.copy(alpha = 0.035f),
            start = Offset(
                width * 0.15f,
                roadY + 18.dp.toPx()
            ),
            end = Offset(
                width * 0.85f,
                roadY + 18.dp.toPx()
            ),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        /*
         * ========================================================
         * ROAD MARKINGS
         * ========================================================
         */

        val dashWidth =
            18.dp.toPx()

        val dashGap =
            21.dp.toPx()

        var dashX =
            width * 0.09f

        while (dashX < width * 0.90f) {

            drawLine(
                color = FlowGreen.copy(alpha = 0.075f),
                start = Offset(
                    dashX,
                    roadY + 12.dp.toPx()
                ),
                end = Offset(
                    dashX + dashWidth,
                    roadY + 12.dp.toPx()
                ),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )

            dashX +=
                dashWidth +
                        dashGap
        }

        /*
         * ========================================================
         * DESTINATION DOCK
         * ========================================================
         */

        drawLine(
            color = FlowGreenDark.copy(alpha = 0.18f),
            start = Offset(
                destinationX,
                roadY - 7.dp.toPx()
            ),
            end = Offset(
                destinationX,
                roadY + 4.dp.toPx()
            ),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawCircle(
            color = FlowBackgroundTop,
            radius = 8.dp.toPx(),
            center = destinationCenter
        )

        drawCircle(
            color = FlowGreenDark.copy(alpha = 0.20f),
            radius = 7.dp.toPx(),
            center = destinationCenter
        )

        drawCircle(
            color = FlowGreenDark,
            radius = 2.8.dp.toPx(),
            center = destinationCenter
        )

        /*
         * ========================================================
         * MOTION TRAIL
         * ========================================================
         */

        if (safeProgress > 0.015f) {

            val trailStrength =
                0.035f +
                        (0.065f * safeProgress)

            val rearX =
                truckX -
                        truckWidth * 0.30f

            /*
             * Long speed line.
             */
            drawLine(
                color = FlowGreen.copy(
                    alpha = trailStrength
                ),
                start = Offset(
                    rearX - 48.dp.toPx(),
                    truckTop + truckHeight * 0.48f
                ),
                end = Offset(
                    rearX - 10.dp.toPx(),
                    truckTop + truckHeight * 0.48f
                ),
                strokeWidth = 1.4.dp.toPx(),
                cap = StrokeCap.Round
            )

            /*
             * Secondary speed line.
             */
            drawLine(
                color = FlowGreen.copy(
                    alpha = trailStrength * 0.65f
                ),
                start = Offset(
                    rearX - 67.dp.toPx(),
                    truckTop + truckHeight * 0.68f
                ),
                end = Offset(
                    rearX - 30.dp.toPx(),
                    truckTop + truckHeight * 0.68f
                ),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )

            /*
             * Upper speed line.
             */
            drawLine(
                color = FlowGreen.copy(
                    alpha = trailStrength * 0.45f
                ),
                start = Offset(
                    rearX - 55.dp.toPx(),
                    truckTop + truckHeight * 0.25f
                ),
                end = Offset(
                    rearX - 30.dp.toPx(),
                    truckTop + truckHeight * 0.25f
                ),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        /*
         * ========================================================
         * TRUCK
         * ========================================================
         */

        drawFleetTruck(
            x = truckX,
            top = truckTop,
            width = truckWidth,
            height = truckHeight,
            wheelRotation = safeProgress * 25f,
            arrival = arrival
        )

        /*
         * ========================================================
         * LIVE TELEMETRY
         * ========================================================
         */

        if (safeProgress > 0.05f) {

            val signalAlpha =
                if (safeProgress > 0.80f) {
                    0.32f
                } else {
                    0.20f
                }

            val signalCenter =
                Offset(
                    truckX + truckWidth * 0.72f,
                    truckTop - 10.dp.toPx()
                )

            drawCircle(
                color = FlowGreen.copy(
                    alpha = signalAlpha
                ),
                radius = 2.3.dp.toPx(),
                center = signalCenter
            )

            drawArc(
                color = FlowGreen.copy(
                    alpha = signalAlpha * 0.55f
                ),
                startAngle = 215f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(
                    signalCenter.x - 7.dp.toPx(),
                    signalCenter.y - 7.dp.toPx()
                ),
                size = androidx.compose.ui.geometry.Size(
                    14.dp.toPx(),
                    14.dp.toPx()
                ),
                style = Stroke(
                    width = 1.dp.toPx()
                )
            )
        }
    }
}


/*
 * ============================================================
 * TRUCK
 * ============================================================
 */

private fun DrawScope.drawFleetTruck(
    x: Float,
    top: Float,
    width: Float,
    height: Float,
    wheelRotation: Float,
    arrival: Float
) {
    /*
     * ========================================================
     * DIMENSIONS
     * ========================================================
     */

    val bodyHeight =
        height * 0.63f

    val cargoWidth =
        width * 0.66f

    val wheelRadius =
        height * 0.185f

    val bodyTop =
        top + height * 0.16f

    val bodyBottom =
        bodyTop + bodyHeight

    /*
     * ========================================================
     * SHADOW
     * ========================================================
     */

    drawLine(
        color = Color.Black.copy(alpha = 0.035f),
        start = Offset(
            x + width * 0.08f,
            bodyBottom + wheelRadius + 4.dp.toPx()
        ),
        end = Offset(
            x + width * 0.94f,
            bodyBottom + wheelRadius + 4.dp.toPx()
        ),
        strokeWidth = 6.dp.toPx(),
        cap = StrokeCap.Round
    )

    /*
     * ========================================================
     * CARGO BODY
     * ========================================================
     */

    drawRoundRect(
        color = FlowGreenDark,
        topLeft = Offset(
            x,
            bodyTop
        ),
        size = androidx.compose.ui.geometry.Size(
            cargoWidth,
            bodyHeight
        ),
        cornerRadius = CornerRadius(
            5.dp.toPx(),
            5.dp.toPx()
        )
    )

    /*
     * Top cargo highlight.
     */
    drawRoundRect(
        color = FlowGreen,
        topLeft = Offset(
            x + 2.dp.toPx(),
            bodyTop + 2.dp.toPx()
        ),
        size = androidx.compose.ui.geometry.Size(
            cargoWidth - 4.dp.toPx(),
            bodyHeight * 0.20f
        ),
        cornerRadius = CornerRadius(
            3.dp.toPx(),
            3.dp.toPx()
        )
    )

    /*
     * Cargo lower shading.
     */
    drawRect(
        color = Color.Black.copy(alpha = 0.025f),
        topLeft = Offset(
            x,
            bodyTop + bodyHeight * 0.72f
        ),
        size = androidx.compose.ui.geometry.Size(
            cargoWidth,
            bodyHeight * 0.28f
        )
    )

    /*
     * ========================================================
     * CARGO PANEL LINES
     * ========================================================
     */

    drawLine(
        color = Color.White.copy(alpha = 0.14f),
        start = Offset(
            x + cargoWidth * 0.10f,
            bodyTop + bodyHeight * 0.30f
        ),
        end = Offset(
            x + cargoWidth * 0.10f,
            bodyBottom - bodyHeight * 0.10f
        ),
        strokeWidth = 1.dp.toPx()
    )

    drawLine(
        color = Color.White.copy(alpha = 0.075f),
        start = Offset(
            x + cargoWidth * 0.50f,
            bodyTop + bodyHeight * 0.30f
        ),
        end = Offset(
            x + cargoWidth * 0.50f,
            bodyBottom - bodyHeight * 0.10f
        ),
        strokeWidth = 1.dp.toPx()
    )

    /*
     * ========================================================
     * MINI FLOW MARK
     * ========================================================
     */

    val markCenter =
        Offset(
            x + cargoWidth * 0.31f,
            bodyTop + bodyHeight * 0.56f
        )

    drawCircle(
        color = Color.White.copy(alpha = 0.10f),
        radius = height * 0.105f,
        center = markCenter
    )

    drawCircle(
        color = Color.White.copy(alpha = 0.22f),
        radius = height * 0.055f,
        center = markCenter
    )

    /*
     * ========================================================
     * CABIN
     * ========================================================
     */

    val cabinLeft =
        x + cargoWidth - 2.dp.toPx()

    val cabinTop =
        top + height * 0.28f

    val cabinRight =
        x + width * 0.96f

    val cabinBottom =
        bodyBottom

    drawRoundRect(
        color = FlowGreen,
        topLeft = Offset(
            cabinLeft,
            cabinTop
        ),
        size = androidx.compose.ui.geometry.Size(
            cabinRight - cabinLeft,
            cabinBottom - cabinTop
        ),
        cornerRadius = CornerRadius(
            5.dp.toPx(),
            5.dp.toPx()
        )
    )

    /*
     * ========================================================
     * WINDOW
     * ========================================================
     */

    val windowLeft =
        cabinLeft + 4.dp.toPx()

    val windowTop =
        cabinTop + 4.dp.toPx()

    val windowRight =
        cabinRight - 4.dp.toPx()

    val windowBottom =
        cabinTop +
                (
                        (cabinBottom - cabinTop) *
                                0.48f
                        )

    drawRoundRect(
        color = FlowBackgroundTop.copy(alpha = 0.91f),
        topLeft = Offset(
            windowLeft,
            windowTop
        ),
        size = androidx.compose.ui.geometry.Size(
            windowRight - windowLeft,
            windowBottom - windowTop
        ),
        cornerRadius = CornerRadius(
            3.dp.toPx(),
            3.dp.toPx()
        )
    )

    /*
     * Window reflection.
     */
    drawLine(
        color = FlowGreen.copy(alpha = 0.18f),
        start = Offset(
            windowLeft + 3.dp.toPx(),
            windowBottom - 2.dp.toPx()
        ),
        end = Offset(
            windowRight - 4.dp.toPx(),
            windowTop + 3.dp.toPx()
        ),
        strokeWidth = 1.dp.toPx()
    )

    /*
     * ========================================================
     * HEADLIGHT
     * ========================================================
     */

    val headlight =
        Offset(
            cabinRight - 2.dp.toPx(),
            cabinTop +
                    (
                            (cabinBottom - cabinTop) *
                                    0.70f
                            )
        )

    drawCircle(
        color = Color.White.copy(alpha = 0.10f),
        radius = 7.dp.toPx(),
        center = headlight
    )

    drawCircle(
        color = Color.White.copy(alpha = 0.90f),
        radius = 2.2.dp.toPx(),
        center = headlight
    )

    /*
     * ========================================================
     * REAR LIGHT
     * ========================================================
     */

    drawCircle(
        color = Color(0xFF9FE2C8).copy(alpha = 0.75f),
        radius = 1.8.dp.toPx(),
        center = Offset(
            x + 2.dp.toPx(),
            bodyTop + bodyHeight * 0.67f
        )
    )

    /*
     * ========================================================
     * WHEELS
     * ========================================================
     */

    val rearWheel =
        Offset(
            x + width * 0.25f,
            bodyBottom + 1.dp.toPx()
        )

    val frontWheel =
        Offset(
            x + width * 0.76f,
            bodyBottom + 1.dp.toPx()
        )

    drawTruckWheel(
        center = rearWheel,
        radius = wheelRadius,
        rotation = wheelRotation
    )

    drawTruckWheel(
        center = frontWheel,
        radius = wheelRadius,
        rotation = wheelRotation
    )

    /*
     * ========================================================
     * ARRIVAL HIGHLIGHT
     * ========================================================
     */

    if (arrival > 0f) {

        val highlightAlpha =
            0.12f * arrival

        drawLine(
            color = FlowGreenLight.copy(
                alpha = highlightAlpha
            ),
            start = Offset(
                x + width * 0.05f,
                bodyTop - 1.dp.toPx()
            ),
            end = Offset(
                x + width * 0.87f,
                bodyTop - 1.dp.toPx()
            ),
            strokeWidth = 1.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}


/*
 * ============================================================
 * WHEEL
 * ============================================================
 */

private fun DrawScope.drawTruckWheel(
    center: Offset,
    radius: Float,
    rotation: Float
) {
    /*
     * Outer tire.
     */
    drawCircle(
        color = FlowInk,
        radius = radius,
        center = center
    )

    /*
     * Inner wheel.
     */
    drawCircle(
        color = FlowSoft,
        radius = radius * 0.43f,
        center = center
    )

    /*
     * Hub.
     */
    drawCircle(
        color = FlowGreenDark,
        radius = radius * 0.17f,
        center = center
    )

    /*
     * Rotating spoke.
     */
    val angle =
        Math.toRadians(
            rotation.toDouble()
        )

    val spokeStart =
        Offset(
            center.x +
                    (
                            cos(angle) *
                                    radius * 0.18f
                            ).toFloat(),
            center.y +
                    (
                            sin(angle) *
                                    radius * 0.18f
                            ).toFloat()
        )

    val spokeEnd =
        Offset(
            center.x +
                    (
                            cos(angle) *
                                    radius * 0.40f
                            ).toFloat(),
            center.y +
                    (
                            sin(angle) *
                                    radius * 0.40f
                            ).toFloat()
        )

    drawLine(
        color = FlowInk.copy(alpha = 0.40f),
        start = spokeStart,
        end = spokeEnd,
        strokeWidth = 1.dp.toPx(),
        cap = StrokeCap.Round
    )
}