package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StudyMateOwl(
    expression: MascotExpression = MascotExpression.Happy,
    modifier: Modifier = Modifier.size(120.dp),
    onClick: (() -> Unit)? = null
) {
    // Continuous subtle floating animation for floating/vibrant Duolingo character look
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_vibe")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Gentle side-to-side body swaying
    val tiltAngle by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tilt"
    )

    // Wing flap animation
    val flapProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (expression == MascotExpression.Celebrating) 220 else 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flap"
    )

    // Interactive scale and rotation states when tapped
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }

    val blinkState = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((3000..6000).random().toLong())
            blinkState.value = true
            delay(150)
            blinkState.value = false
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = offsetY.dp.toPx()
                rotationZ = tiltAngle + rotation.value
                scaleX = scale.value
                scaleY = scale.value
            }
            .clickable {
                if (onClick != null) {
                    onClick.invoke()
                } else {
                    // Trigger a playful custom interactive animation block on click
                    coroutineScope.launch {
                        scale.animateTo(1.22f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                        scale.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                    }
                    coroutineScope.launch {
                        rotation.animateTo(360f, animationSpec = tween(550, easing = FastOutSlowInEasing))
                        rotation.snapTo(0f)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Owl main body rounded shape (Duolingo Green #58CC02 style)
            val bodyColor = Color(0xFF58CC02)
            val lightBodyColor = Color(0xFF78E20F)
            val bellyColor = Color(0xFFFFFFFF)
            val beakColor = Color(0xFFFF9600)
            val feetColor = Color(0xFFE97600)
            val glassesColor = Color(0xFF3C3C3C)

            // Draw shadow
            drawOval(
                color = Color(0x15000000),
                topLeft = Offset(width * 0.15f, height * 0.82f),
                size = Size(width * 0.7f, height * 0.12f)
            )

            // Draw Feet
            drawOval(
                color = feetColor,
                topLeft = Offset(width * 0.28f, height * 0.8f),
                size = Size(width * 0.18f, height * 0.1f)
            )
            drawOval(
                color = feetColor,
                topLeft = Offset(width * 0.54f, height * 0.8f),
                size = Size(width * 0.18f, height * 0.1f)
            )

            // Draw Body (rounded container)
            drawRoundRect(
                color = bodyColor,
                topLeft = Offset(width * 0.12f, height * 0.15f),
                size = Size(width * 0.76f, height * 0.7f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.35f, height * 0.35f)
            )

            // Draw Horns (ears style triangle overlay)
            val hornPathL = Path().apply {
                moveTo(width * 0.2f, height * 0.25f)
                lineTo(width * 0.12f, height * 0.12f)
                lineTo(width * 0.42f, height * 0.2f)
                close()
            }
            drawPath(hornPathL, color = bodyColor)

            val hornPathR = Path().apply {
                moveTo(width * 0.8f, height * 0.25f)
                lineTo(width * 0.88f, height * 0.12f)
                lineTo(width * 0.58f, height * 0.2f)
                close()
            }
            drawPath(hornPathR, color = bodyColor)

            // Draw Wings with beautiful live flight flaps
            // Left wing flap
            val leftWingPath = Path().apply {
                val controlY = height * (0.55f - (flapProgress * 0.18f))
                val endY = height * (0.7f - (flapProgress * 0.08f))
                moveTo(width * 0.14f, height * 0.45f)
                quadraticBezierTo(
                    width * (0.04f - (flapProgress * 0.05f)), controlY,
                    width * 0.14f, endY
                )
                close()
            }
            drawPath(leftWingPath, color = Color(0xFF46A302))

            // Right wing flap (celebrates can flap high)
            val rightWingPath = Path().apply {
                val controlY = height * (0.55f - (flapProgress * 0.18f))
                val endY = height * (0.7f - (flapProgress * 0.08f))
                if (expression == MascotExpression.Celebrating) {
                    moveTo(width * 0.86f, height * 0.45f)
                    quadraticBezierTo(
                        width * (0.96f + (flapProgress * 0.08f)), height * (0.3f - (flapProgress * 0.15f)),
                        width * (0.82f - (flapProgress * 0.05f)), height * (0.25f - (flapProgress * 0.1f))
                    )
                } else {
                    moveTo(width * 0.86f, height * 0.45f)
                    quadraticBezierTo(
                        width * (0.96f + (flapProgress * 0.05f)), controlY,
                        width * 0.86f, endY
                    )
                }
                close()
            }
            drawPath(rightWingPath, color = Color(0xFF46A302))

            // Draw White Belly Patch
            drawOval(
                color = bellyColor,
                topLeft = Offset(width * 0.25f, height * 0.45f),
                size = Size(width * 0.5f, height * 0.36f)
            )

            // Belly feathers (chest markings: cute wedges)
            drawArc(
                color = Color(0xFFDCDCDC),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(width * 0.4f, height * 0.52f),
                size = Size(width * 0.08f, height * 0.05f)
            )
            drawArc(
                color = Color(0xFFDCDCDC),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(width * 0.52f, height * 0.52f),
                size = Size(width * 0.08f, height * 0.05f)
            )
            drawArc(
                color = Color(0xFFDCDCDC),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(width * 0.46f, height * 0.62f),
                size = Size(width * 0.08f, height * 0.05f)
            )

            // Draw Eye Patches (Giant white circles)
            val eyePatchRadius = width * 0.18f
            drawCircle(
                color = Color.White,
                radius = eyePatchRadius,
                center = Offset(width * 0.33f, height * 0.38f)
            )
            drawCircle(
                color = Color.White,
                radius = eyePatchRadius,
                center = Offset(width * 0.67f, height * 0.38f)
            )

            // Draw Pupils
            if (blinkState.value) {
                // Drawn as closed eyelids (curved strokes)
                drawArc(
                    color = Color(0xFF333333),
                    startAngle = 10f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(width * 0.23f, height * 0.35f),
                    size = Size(width * 0.2f, height * 0.08f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                )
                drawArc(
                    color = Color(0xFF333333),
                    startAngle = 10f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(width * 0.57f, height * 0.35f),
                    size = Size(width * 0.2f, height * 0.08f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                )
            } else {
                when (expression) {
                    MascotExpression.Encouraging -> {
                        // Left eye winks
                        drawArc(
                            color = Color(0xFF333333),
                            startAngle = 10f,
                            sweepAngle = 160f,
                            useCenter = false,
                            topLeft = Offset(width * 0.23f, height * 0.35f),
                            size = Size(width * 0.2f, height * 0.08f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                        )
                        // Right eye open
                        drawCircle(
                            color = Color(0xFF3C3C3C),
                            radius = width * 0.08f,
                            center = Offset(width * 0.65f, height * 0.38f)
                        )
                        // Eye reflection spark
                        drawCircle(
                            color = Color.White,
                            radius = width * 0.025f,
                            center = Offset(width * 0.63f, height * 0.36f)
                        )
                    }
                    MascotExpression.Celebrating -> {
                        // Both eyes happy curves (^)
                        drawArc(
                            color = Color(0xFF333333),
                            startAngle = 190f,
                            sweepAngle = 160f,
                            useCenter = false,
                            topLeft = Offset(width * 0.23f, height * 0.35f),
                            size = Size(width * 0.18f, height * 0.12f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                        )
                        drawArc(
                            color = Color(0xFF333333),
                            startAngle = 190f,
                            sweepAngle = 160f,
                            useCenter = false,
                            topLeft = Offset(width * 0.58f, height * 0.35f),
                            size = Size(width * 0.18f, height * 0.12f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                        )
                    }
                    else -> {
                        // Standard happy or studious eyes open
                        drawCircle(
                            color = Color(0xFF3C3C3C),
                            radius = width * 0.08f,
                            center = Offset(width * 0.35f, height * 0.38f)
                        )
                        drawCircle(
                            color = Color(0xFF3C3C3C),
                            radius = width * 0.08f,
                            center = Offset(width * 0.65f, height * 0.38f)
                        )
                        // Reflections
                        drawCircle(
                            color = Color.White,
                            radius = width * 0.025f,
                            center = Offset(width * 0.33f, height * 0.36f)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = width * 0.025f,
                            center = Offset(width * 0.63f, height * 0.36f)
                        )
                    }
                }
            }

            // Draw glasses if Studious
            if (expression == MascotExpression.Studious) {
                drawCircle(
                    color = glassesColor,
                    radius = width * 0.19f,
                    center = Offset(width * 0.33f, height * 0.38f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                )
                drawCircle(
                    color = glassesColor,
                    radius = width * 0.19f,
                    center = Offset(width * 0.67f, height * 0.38f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                )
                // Glasses bridge
                drawLine(
                    color = glassesColor,
                    start = Offset(width * 0.44f, height * 0.38f),
                    end = Offset(width * 0.56f, height * 0.38f),
                    strokeWidth = 8f
                )
            }

            // Draw Beak (Orange orange-rounded triangle)
            val beakPath = Path().apply {
                moveTo(width * 0.44f, height * 0.43f)
                lineTo(width * 0.56f, height * 0.43f)
                lineTo(width * 0.5f, height * 0.53f)
                close()
            }
            drawPath(beakPath, color = beakColor)

            // Draw party hat if Celebrating
            if (expression == MascotExpression.Celebrating) {
                val hatPath = Path().apply {
                    moveTo(width * 0.4f, height * 0.16f)
                    lineTo(width * 0.6f, height * 0.16f)
                    lineTo(width * 0.5f, height * 0.02f)
                    close()
                }
                drawPath(hatPath, color = Color(0xFFFF9600))
                drawCircle(
                    color = Color(0xFFFF4B4B),
                    radius = width * 0.03f,
                    center = Offset(width * 0.5f, height * 0.01f)
                )
            }
        }
    }
}

@Composable
fun OwlSpeechBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
        color = Color(0xFFF1FDF0),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF58CC02))
    ) {
        Box(modifier = Modifier.padding(14.dp)) {
            Text(
                text = text,
                color = Color(0xFF3C3C3C),
                fontSize = 14.sp,
                lineHeight = 19.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
        }
    }
}
