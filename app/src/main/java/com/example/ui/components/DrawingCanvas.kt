package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class StrokePath(
    val points: List<Offset>,
    val color: Color,
    val width: Float
)

@Composable
fun InteractiveDrawingCanvas(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    onSaveDrawing: ((String) -> Unit)? = null
) {
    var strokesList = remember { mutableStateListOf<StrokePath>() }
    var currentPoints = remember { mutableStateListOf<Offset>() }
    
    var selectedColor by remember { mutableStateOf(Color(0xFF3C3C3C)) } // standard charcoal
    var selectedWidth by remember { mutableStateOf(6f) }
    var isEraserActive by remember { mutableStateOf(false) }

    val colors = listOf(
        Color(0xFF3C3C3C), // Charcoal
        Color(0xFFFF4B4B), // Red
        Color(0xFF1CB0F6), // Blue
        Color(0xFF58CC02), // Green
        Color(0xFFFF9600), // Orange
        Color(0xFFFFD900)  // Yellow
    )

    Column(
        modifier = modifier
            .background(Color(0xFFF7F7F7), shape = MaterialTheme.shapes.medium)
            .border(2.dp, Color(0xFFE5E5E5), shape = MaterialTheme.shapes.medium)
            .padding(8.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Colors list
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                colors.forEach { color ->
                    IconButton(
                        onClick = {
                            selectedColor = color
                            isEraserActive = false
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .background(color, shape = CircleShape)
                            .border(
                                width = if (selectedColor == color && !isEraserActive) 3.dp else 1.dp,
                                color = if (selectedColor == color && !isEraserActive) Color.White else Color(0x33000000),
                                shape = CircleShape
                            )
                    ) {}
                }
            }

            // Brush tools
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Eraser Mode Toggle Button
                IconButton(
                    onClick = {
                        isEraserActive = !isEraserActive
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isEraserActive) Color(0xFFFFEBF0) else Color.Transparent,
                        contentColor = if (isEraserActive) Color(0xFFFF4B4B) else Color(0xFF777777)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear, // Draw cleaner representation
                        contentDescription = "Eraser Mode",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Width Toggle Button
                IconButton(
                    onClick = {
                        selectedWidth = when (selectedWidth) {
                            6f -> 12f
                            12f -> 24f
                            else -> 6f
                        }
                    }
                ) {
                    val brushLabel = when (selectedWidth) {
                        6f -> "Thin"
                        12f -> "Med"
                        else -> "Thick"
                    }
                    Text(text = brushLabel, fontSize = 11.sp, color = Color(0xFF777777), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }

                // Clear button
                IconButton(
                    onClick = {
                        strokesList.clear()
                        currentPoints.clear()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Canvas",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFFFF4B4B)
                    )
                }
            }
        }

        // Draw Canvas Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(backgroundColor, shape = MaterialTheme.shapes.small)
                .pointerInput(isEraserActive, selectedColor, selectedWidth) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints.add(offset)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            currentPoints.add(change.position)
                        },
                        onDragEnd = {
                            val colorToUse = if (isEraserActive) backgroundColor else selectedColor
                            strokesList.add(StrokePath(currentPoints.toList(), colorToUse, selectedWidth))
                            currentPoints.clear()
                            onSaveDrawing?.invoke("draw_strokes_${strokesList.size}")
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw past paths
                strokesList.forEach { stroke ->
                    val path = Path().apply {
                        if (stroke.points.isNotEmpty()) {
                            moveTo(stroke.points[0].x, stroke.points[0].y)
                            for (i in 1 until stroke.points.size) {
                                lineTo(stroke.points[i].x, stroke.points[i].y)
                            }
                        }
                    }
                    drawPath(
                        path = path,
                        color = stroke.color,
                        style = Stroke(
                            width = stroke.width,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Draw active drawing path
                if (currentPoints.isNotEmpty()) {
                    val activePath = Path().apply {
                        moveTo(currentPoints[0].x, currentPoints[0].y)
                        for (i in 1 until currentPoints.size) {
                            lineTo(currentPoints[i].x, currentPoints[i].y)
                        }
                    }
                    val activeColor = if (isEraserActive) backgroundColor else selectedColor
                    drawPath(
                        path = activePath,
                        color = activeColor,
                        style = Stroke(
                            width = selectedWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}
