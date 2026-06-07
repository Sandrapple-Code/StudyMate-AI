package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A beautiful Duolingo-styled 3D button.
 * Pressing causes a physical downward translation simulating a genuine push tactile mechanism.
 */
@Composable
fun DuolingoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF58CC02), // Duolingo Green
    shadowColor: Color = Color(0xFF46A302), // Darker Green
    contentColor: Color = Color.White,
    height: Int = 48,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    val translationY by animateFloatAsState(targetValue = if (isPressed) 4f else 0f, label = "press")

    val finalColor = if (enabled) color else Color(0xFFE5E5E5)
    val finalShadowColor = if (enabled) shadowColor else Color(0xFFCCCCCC)
    val finalContentColor = if (enabled) contentColor else Color(0xFF888888)

    Box(
        modifier = modifier
            .height(height.dp)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                            onClick()
                        }
                    )
                }
            }
    ) {
        // Shadow Background Plate
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
                .background(finalShadowColor, shape = RoundedCornerShape(18.dp))
        )

        // Top Floating Plate
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 4.dp)
                .offset(y = translationY.dp)
                .background(finalColor, shape = RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                color = finalContentColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
        }
    }
}

/**
 * A cute 3D Card following Duolingo shapes.
 */
@Composable
fun DuolingoCard(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    borderColor: Color = Color(0xFFE2E8F0), // Slate-200 soft border for classic modern Bento Look
    shadowThickness: Int = 4,
    shadowColor: Color = Color(0xFFE2E8F0), // Slate-200 shadow
    shape: RoundedCornerShape = RoundedCornerShape(24.dp), // Bento-style heavily rounded corners
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = com.example.ui.theme.LocalIsDarkMode.current
    val resolvedColor = if (isDark) {
        if (color == Color.White || color == Color(0xFFFFFDF5) || color == Color(0xFFF8FAFC) || color == Color(0xFFFFFBEB)) {
            Color(0xFF1E293B)
        } else {
            color
        }
    } else {
        color
    }

    val resolvedBorderColor = if (isDark) {
        if (borderColor == Color(0xFFE2E8F0) || borderColor == Color(0xFFE5E5E5) || borderColor == Color(0xFFFEF3C7)) {
            Color(0xFF334155)
        } else {
            borderColor
        }
    } else {
        borderColor
    }

    val resolvedShadowColor = if (isDark) {
        Color(0x20000000)
    } else {
        shadowColor
    }

    Box(modifier = modifier) {
        // Dark plate shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = shadowThickness.dp)
                .background(resolvedShadowColor, shape = shape)
        )

        // Card front body
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = shadowThickness.dp)
                .background(resolvedColor, shape = shape)
                .border(2.dp, resolvedBorderColor, shape = shape)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * XP Progress Indicator styled like the Duo progress lines.
 */
@Composable
fun StreakProgressBar(
    xp: Int,
    levelMax: Int = 100,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFF9600) // Orange Gold
) {
    val isDark = com.example.ui.theme.LocalIsDarkMode.current
    val progress = (xp.toFloat() / levelMax).coerceIn(0f, 1f)
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "XP: $xp / $levelMax",
                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF3C3C3C),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Star Progress",
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Progress Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(if (isDark) Color(0xFF334155) else Color(0xFFE5E5E5), shape = RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(color, shape = RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x22000000), shape = RoundedCornerShape(8.dp))
            )
        }
    }
}

/**
 * Streak Fire Badging count
 */
@Composable
fun StreakFireCounter(
    days: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFFFFF5E6), shape = RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFFFF9600), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = "Streak Fire",
            tint = Color(0xFFFF4600),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "$days DAYS",
            color = Color(0xFFFF9600),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
