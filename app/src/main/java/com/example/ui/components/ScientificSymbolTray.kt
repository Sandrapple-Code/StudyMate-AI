package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScientificItem
import com.example.data.ScientificSymbols

/**
 * A beautiful, highly helpful Scientific Symbols and Formula Keyboard Assistant with expandable tabs.
 * Allows instant injection of complex characters/equations.
 */
@Composable
fun ScientificSymbolTray(
    modifier: Modifier = Modifier,
    onSymbolSelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("Mathematics") }
    var hoveringInfo by remember { mutableStateOf<ScientificItem?>(null) }

    val categories = listOf("Mathematics", "Physics", "Chemistry")
    val filteredItems = ScientificSymbols.items.filter { it.category == selectedCategory }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(16.dp))
            .background(Color.White, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = "Symbols",
                        tint = Color(0xFF8E79FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Ω SCIENTIFIC FORMULAS & SYMBOLS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8E79FF),
                        letterSpacing = 0.8.sp
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle Expand",
                    tint = Color(0xFF64748B)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category Tab Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = selectedCategory == category
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) Color(0xFFF1FDF0) else Color(0xFFF8FAFC)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFF58CC02) else Color(0xFFCBD5E1),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedCategory = category
                                        hoveringInfo = null
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color(0xFF58CC02) else Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // Horizontal Scrolling symbols buttons
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredItems) { item ->
                            Button(
                                onClick = {
                                    onSymbolSelected(item.char)
                                    hoveringInfo = item
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    contentColor = Color(0xFF0F172A)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text(
                                    text = item.char,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Contextual definition details panel
                    val displayItem = hoveringInfo ?: filteredItems.firstOrNull()
                    if (displayItem != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Formula Information",
                                tint = Color(0xFF1CB0F6),
                                modifier = Modifier.size(16.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = displayItem.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = displayItem.usage,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            // Show LaTeX prompt
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE2E8F0), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    // Make click copy latex expression
                                    .clickable { onSymbolSelected(displayItem.char) }
                            ) {
                                Text(
                                    text = "LaTeX: ${displayItem.latex}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
