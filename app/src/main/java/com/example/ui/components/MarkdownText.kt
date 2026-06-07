package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A simple yet high-fidelity native Markdown renderer for Jetpack Compose.
 * Supports: Headers (#, ##, ###), Bullet list items (*, -), Code Blocks (```), Bold text (**bold**), and Divider lines (---)
 */
@Composable
fun MarkdownText(
    markdownString: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFF3C3C3C),
    isOverLightBackground: Boolean = false
) {
    val isDark = com.example.ui.theme.LocalIsDarkMode.current
    val textColor = if (isDark && !isOverLightBackground) {
        if (textColor == Color(0xFF3C3C3C) || textColor == Color(0xFF141C24) || textColor == Color(0xFF1E293B) || textColor == Color.Unspecified) {
            Color(0xFFF1F5F9)
        } else {
            textColor
        }
    } else {
        textColor
    }

    val lines = markdownString.split("\n")
    var inCodeBlock = false
    var currentCodeBlock = StringBuilder()
    
    // Buffer to group adjacent table lines together
    val tableBuffer = remember { mutableStateListOf<String>() }

    @Composable
    fun FlushTableBuffer() {
        if (tableBuffer.isNotEmpty()) {
            val list = tableBuffer.toList()
            tableBuffer.clear()
            RenderMarkdownTable(tableRows = list, textColor = textColor)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (line in lines) {
            val trimmedLine = line.trim()

            // 1. Check Table Line bounds
            val isTableLine = !inCodeBlock && trimmedLine.startsWith("|") && trimmedLine.endsWith("|")
            if (isTableLine) {
                tableBuffer.add(trimmedLine)
                continue
            } else {
                // If we hit a non-table line, first render any buffered table lines
                if (tableBuffer.isNotEmpty()) {
                    val list = tableBuffer.toList()
                    tableBuffer.clear()
                    RenderMarkdownTable(tableRows = list, textColor = textColor)
                }
            }

            // 2. Handle Code block bounds
            if (trimmedLine.startsWith("```")) {
                if (inCodeBlock) {
                    // Render the accumulated code block
                    CodeBlock(code = currentCodeBlock.toString().trim())
                    currentCodeBlock = StringBuilder()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                continue
            }

            if (inCodeBlock) {
                currentCodeBlock.append(line).append("\n")
                continue
            }

            // 3. Horizontal Rule Divider
            if (trimmedLine == "---" || trimmedLine == "***") {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color(0xFFE5E5E5), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                continue
            }

            // 4. Headers
            if (trimmedLine.startsWith("# ")) {
                val headerText = trimmedLine.drop(2)
                Text(
                    text = renderFormattedSpans(headerText, textColor),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1CB0F6), // Main Blue
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
                continue
            }
            if (trimmedLine.startsWith("## ")) {
                val headerText = trimmedLine.drop(3)
                Text(
                    text = renderFormattedSpans(headerText, textColor),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF58CC02), // Main Green
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                continue
            }
            if (trimmedLine.startsWith("### ")) {
                val headerText = trimmedLine.drop(4)
                Text(
                    text = renderFormattedSpans(headerText, textColor),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9600), // Main Orange
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
                continue
            }

            // 5. Bullet lists
            if (trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ")) {
                val itemText = trimmedLine.drop(2)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 2.dp, bottom = 2.dp)
                ) {
                    Text(
                        text = "• ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF58CC02)
                    )
                    Text(
                        text = renderFormattedSpans(itemText, textColor),
                        fontSize = 14.sp,
                        color = textColor,
                        lineHeight = 18.sp
                    )
                }
                continue
            }

            // 6. Standard line paragraph
            if (trimmedLine.isNotEmpty()) {
                Text(
                    text = renderFormattedSpans(trimmedLine, textColor),
                    fontSize = 14.sp,
                    color = textColor,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Final buffer flush when document finishes
        if (tableBuffer.isNotEmpty()) {
            val list = tableBuffer.toList()
            tableBuffer.clear()
            RenderMarkdownTable(tableRows = list, textColor = textColor)
        }
    }
}

/**
 * Renders parsed Markdown tables natively with proper aesthetics.
 */
@Composable
fun RenderMarkdownTable(tableRows: List<String>, textColor: Color) {
    val tableCellTextColor = Color(0xFF1E293B)
    // Each row in tableRows is a raw line starting/ending with |
    val parsedRows = tableRows.map { row ->
        val parts = row.split("|").map { it.trim() }
        val cells = if (parts.size >= 2) {
            parts.subList(1, parts.size - 1)
        } else {
            emptyList()
        }
        cells
    }.filter { it.isNotEmpty() }

    if (parsedRows.isEmpty()) return

    // Identify if the second row is a divider (usually contains only hyphens, colons, spaces)
    val hasDivider = parsedRows.size > 1 && parsedRows[1].all { cell ->
        cell.all { it == '-' || it == ':' || it.isWhitespace() }
    }
    
    val displayRows = if (hasDivider) {
        parsedRows.filterIndexed { index, _ -> index != 1 }
    } else {
        parsedRows
    }

    if (displayRows.isEmpty()) return

    val columnCount = displayRows.maxOf { it.size }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color(0xFFCBD5E1), shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        displayRows.forEachIndexed { rowIndex, cells ->
            val isHeader = hasDivider && rowIndex == 0
            val rowBg = if (isHeader) Color(0xFFF1F5F9) else if (rowIndex % 2 == 0) Color.White else Color(0xFFF8FAFC)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rowBg)
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (colIndex in 0 until columnCount) {
                    val cellText = cells.getOrNull(colIndex) ?: ""
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = renderFormattedSpans(cellText, tableCellTextColor),
                            fontWeight = if (isHeader) FontWeight.Black else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isHeader) Color(0xFF0F172A) else tableCellTextColor
                        )
                    }
                }
            }
            if (rowIndex < displayRows.lastIndex) {
                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            }
        }
    }
}

/**
 * Text styling parser helper mapping **bold** or *italic* substrings to AnnotatedStrings, including math symbols translation.
 */
@Composable
private fun renderFormattedSpans(text: String, baseColor: Color) = buildAnnotatedString {
    val translatedText = formatScientificText(text)
    var index = 0
    while (index < translatedText.length) {
        val nextDoubleAsterisk = translatedText.indexOf("**", index)
        if (nextDoubleAsterisk != -1) {
            // Append preceding regular text
            if (nextDoubleAsterisk > index) {
                append(translatedText.substring(index, nextDoubleAsterisk))
            }
            // Find closing double asterisk
            val closingDoubleAsterisk = translatedText.indexOf("**", nextDoubleAsterisk + 2)
            if (closingDoubleAsterisk != -1) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, color = baseColor)) {
                    append(translatedText.substring(nextDoubleAsterisk + 2, closingDoubleAsterisk))
                }
                index = closingDoubleAsterisk + 2
            } else {
                append("**")
                index = nextDoubleAsterisk + 2
            }
        } else {
            // No more bold spans
            append(translatedText.substring(index))
            break
        }
    }
}

/**
 * Parses and translates LaTeX commands, superscripts (^), subscripts (_), division symbols,
 * and scientific equations to legible Unicode math components.
 */
fun formatScientificText(input: String): String {
    var text = input

    // Translate LaTeX fractions: \frac{num}{den} -> (num) / (den)
    val fracRegex = Regex("""\\frac\{([^}]+)\}\{([^}]+)\}""")
    text = fracRegex.replace(text) { match ->
        "(${match.groupValues[1]})/(${match.groupValues[2]})"
    }

    // Translate LaTeX square root: \sqrt{content} -> √(content)
    val sqrtRegex = Regex("""\\sqrt\{([^}]+)\}""")
    text = sqrtRegex.replace(text) { match ->
        "√(${match.groupValues[1]})"
    }

    val sqrtSimpleRegex = Regex("""\\sqrt\s+(\w+)""")
    text = sqrtSimpleRegex.replace(text) { match ->
        "√(${match.groupValues[1]})"
    }

    // Comprehensive list of standard scientific and mathematical translations
    val latexReplacements = mapOf(
        "\\alpha" to "α",
        "\\beta" to "β",
        "\\gamma" to "γ",
        "\\theta" to "θ",
        "\\pi" to "π",
        "\\sum" to "Σ",
        "\\int" to "∫",
        "\\Delta" to "Δ",
        "\\sigma" to "σ",
        "\\mu" to "μ",
        "\\lambda" to "λ",
        "\\omega" to "ω",
        "\\phi" to "φ",
        "\\infty" to "∞",
        "\\approx" to "≈",
        "\\neq" to "≠",
        "\\pm" to "±",
        "\\rightarrow" to "→",
        "\\partial" to "∂",
        "\\nabla" to "∇",
        "\\hbar" to "ℏ",
        "\\Psi" to "Ψ",
        "\\nu" to "ν"
    )

    for ((latex, symbol) in latexReplacements) {
        text = text.replace(latex, symbol)
    }

    // Parse curly-braced exponents/superscripts: x^{2} or x^{abc}
    val superCurly = Regex("""\^\{([^}]+)\}""")
    text = superCurly.replace(text) { match ->
        convertToSuperscript(match.groupValues[1])
    }

    // Parse individual character exponent/superscripts: x^2 or e^x
    val superSingle = Regex("""\^([0-9a-zA-Z+-])""")
    text = superSingle.replace(text) { match ->
        convertToSuperscript(match.groupValues[1])
    }

    // Parse curly-braced subscripts: H_{12}
    val subCurly = Regex("""_\{([^}]+)\}""")
    text = subCurly.replace(text) { match ->
        convertToSubscript(match.groupValues[1])
    }

    // Parse individual character subscripts: H_2
    val subSingle = Regex("""_([0-9a-zA-Z+-])""")
    text = subSingle.replace(text) { match ->
        convertToSubscript(match.groupValues[1])
    }

    // General symbol polish
    text = text.replace("\\*", "×")

    return text
}

fun convertToSuperscript(input: String): String {
    val map = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', 'n' to 'ⁿ', 'x' to 'ˣ',
        'y' to 'ʸ', 'i' to 'ⁱ', 'a' to 'ᵃ', 'b' to 'ᵇ', 'c' to 'ᶜ',
        'd' to 'ᵈ', 'e' to 'ᵉ', 'f' to 'ᶠ', 'g' to 'ᵍ', 'h' to 'ʰ',
        'j' to 'ʲ', 'k' to 'ᵏ', 'l' to 'ˡ', 'm' to 'ᵐ', 'o' to 'ᵒ',
        'p' to 'ᵖ', 'r' to 'ʳ', 's' to 'ˢ', 't' to 'ᵗ', 'u' to 'ᵘ',
        'v' to 'ᵛ', 'w' to 'ʷ', 'z' to 'ᶻ'
    )
    return input.map { map[it] ?: it }.joinToString("")
}

fun convertToSubscript(input: String): String {
    val map = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '+' to '₊', '-' to '₋', '=' to '₌', 'a' to 'ₐ', 'e' to 'ₑ',
        'h' to 'ₕ', 'i' to 'ᵢ', 'j' to 'ⱼ', 'k' to 'ₖ', 'l' to 'ₗ',
        'm' to 'ₘ', 'n' to 'ₙ', 'o' to 'ₒ', 'p' to 'ₚ', 'r' to 'ᵣ',
        's' to 'ₛ', 't' to 'ₜ', 'u' to 'ᵤ', 'v' to 'ᵥ', 'x' to 'ₓ',
        'y' to 'ᵧ'
    )
    return input.map { map[it] ?: it }.joinToString("")
}

@Composable
fun CodeBlock(code: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color(0xFF3C3C3C), shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF2B2B2B), shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            color = Color(0xFFECECEC),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}
