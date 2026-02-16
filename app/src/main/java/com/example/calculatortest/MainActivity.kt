package com.example.calculatortest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculatortest.ui.theme.CalculatorTestTheme
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTestTheme {
                CalculatorScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun CalculatorScreen(modifier: Modifier = Modifier) {
    var currentInput by rememberSaveable { mutableStateOf("0") }
    var storedValue by rememberSaveable { mutableStateOf<Double?>(null) }
    var storedText by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingOperator by rememberSaveable { mutableStateOf<String?>(null) }
    var startNewInput by rememberSaveable { mutableStateOf(true) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun operatorSymbol(op: String): String {
        return when (op) {
            "*" -> "×"
            "/" -> "÷"
            "-" -> "−"
            else -> op
        }
    }

    fun displayText(): String {
        if (error != null) return "Error"
        if (pendingOperator != null) {
            val lhs = storedText ?: "0"
            val rhs = if (startNewInput) "" else currentInput
            return lhs + operatorSymbol(pendingOperator ?: "") + rhs
        }
        return currentInput
    }

    fun clearAll() {
        currentInput = "0"
        storedValue = null
        storedText = null
        pendingOperator = null
        startNewInput = true
        error = null
    }

    fun applyOperation(lhs: Double, rhs: Double, op: String): Double? {
        return when (op) {
            "+" -> lhs + rhs
            "-" -> lhs - rhs
            "*" -> lhs * rhs
            "/" -> {
                if (rhs == 0.0) {
                    error = "Cannot divide by zero"
                    null
                } else {
                    lhs / rhs
                }
            }
            else -> rhs
        }
    }

    fun formatNumber(value: Double): String {
        if (value == 0.0) return "0"
        if (value % 1.0 == 0.0) return value.toLong().toString()
        return value.toString().trimEnd('0').trimEnd('.')
    }

    fun appendDigit(digit: String) {
        if (error != null) clearAll()
        currentInput = if (startNewInput || currentInput == "0") digit else currentInput + digit
        startNewInput = false
    }

    fun appendDecimal() {
        if (error != null) clearAll()
        if (startNewInput) {
            currentInput = "0."
            startNewInput = false
        } else if (!currentInput.contains(".")) {
            currentInput += "."
        }
    }

    fun backspace() {
        if (error != null) {
            clearAll()
            return
        }
        if (pendingOperator != null && startNewInput) {
            currentInput = storedText ?: currentInput
            storedValue = null
            storedText = null
            pendingOperator = null
            startNewInput = false
            return
        }
        if (startNewInput) return
        currentInput = if (currentInput.length == 1) {
            startNewInput = true
            "0"
        } else {
            currentInput.dropLast(1)
        }
    }

    fun onOperator(op: String) {
        if (error != null) return
        val currentValue = currentInput.toDoubleOrNull() ?: return
        if (pendingOperator == null) {
            storedValue = currentValue
            storedText = currentInput
            pendingOperator = op
            startNewInput = true
            return
        }
        if (startNewInput) {
            pendingOperator = op
            return
        }
        val lhs = storedValue ?: return
        val result = applyOperation(lhs, currentValue, pendingOperator ?: op) ?: return
        val formatted = formatNumber(result)
        storedValue = result
        storedText = formatted
        currentInput = formatted
        pendingOperator = op
        startNewInput = true
    }

    fun onEquals() {
        if (error != null) return
        val lhs = storedValue ?: return
        val op = pendingOperator ?: return
        if (startNewInput) return
        val rhs = currentInput.toDoubleOrNull() ?: return
        val result = applyOperation(lhs, rhs, op) ?: return
        val formatted = formatNumber(result)
        currentInput = formatted
        storedValue = result
        storedText = null
        pendingOperator = null
        startNewInput = true
    }

    fun onSquareRoot() {
        if (error != null) return
        val value = currentInput.toDoubleOrNull() ?: return
        if (value < 0.0) {
            error = "Error"
            return
        }
        val formatted = formatNumber(sqrt(value))
        currentInput = formatted
        if (pendingOperator != null && startNewInput) {
            storedValue = formatted.toDoubleOrNull()
            storedText = formatted
        } else if (pendingOperator == null) {
            storedValue = null
            storedText = null
            startNewInput = true
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Jan's Calculator",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 8.dp),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    AdaptiveDisplayText(text = displayText(), fontSize = 48.sp)
                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("C", Modifier.weight(1f), onClick = { clearAll() })
                CalcButton("⌫", Modifier.weight(1f), onClick = { backspace() })
                CalcButton(
                    "√",
                    Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { onSquareRoot() }
                )
                CalcButton(
                    "+",
                    Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { onOperator("+") }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("7", Modifier.weight(1f), onClick = { appendDigit("7") })
                CalcButton("8", Modifier.weight(1f), onClick = { appendDigit("8") })
                CalcButton("9", Modifier.weight(1f), onClick = { appendDigit("9") })
                CalcButton(
                    "−",
                    Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { onOperator("-") }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("4", Modifier.weight(1f), onClick = { appendDigit("4") })
                CalcButton("5", Modifier.weight(1f), onClick = { appendDigit("5") })
                CalcButton("6", Modifier.weight(1f), onClick = { appendDigit("6") })
                CalcButton(
                    "×",
                    Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { onOperator("*") }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("1", Modifier.weight(1f), onClick = { appendDigit("1") })
                CalcButton("2", Modifier.weight(1f), onClick = { appendDigit("2") })
                CalcButton("3", Modifier.weight(1f), onClick = { appendDigit("3") })
                CalcButton(
                    "÷",
                    Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { onOperator("/") }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("0", Modifier.weight(2f), onClick = { appendDigit("0") })
                CalcButton(".", Modifier.weight(1f), onClick = { appendDecimal() })
                CalcButton(
                    "=",
                    Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { onEquals() }
                )
            }
        }
    }
}

private val numericDisplayRegex = Regex("^-?(?:\\d+\\.?\\d*|\\.\\d+)$")

@Composable
private fun AdaptiveDisplayText(text: String, fontSize: androidx.compose.ui.unit.TextUnit) {
    val textStyle = TextStyle(fontSize = fontSize)
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val availableWidthPx = with(density) { maxWidth.toPx() }.toInt()
        val adaptedText = remember(text, availableWidthPx) {
            adaptNumericTextToWidth(
                rawText = text,
                availableWidthPx = availableWidthPx,
                measureWidth = { candidate ->
                    textMeasurer.measure(
                        text = AnnotatedString(candidate),
                        style = textStyle,
                        maxLines = 1,
                        softWrap = false
                    ).size.width
                }
            )
        }

        Text(
            text = adaptedText,
            modifier = Modifier.fillMaxWidth(),
            style = textStyle,
            textAlign = TextAlign.End,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
    }
}

private fun adaptNumericTextToWidth(
    rawText: String,
    availableWidthPx: Int,
    measureWidth: (String) -> Int
): String {
    if (rawText.endsWith(".")) return rawText
    if (!numericDisplayRegex.matches(rawText)) return rawText
    if (measureWidth(rawText) <= availableWidthPx) return rawText

    val value = rawText.toBigDecimalOrNull() ?: return rawText

    for (sigDigits in 16 downTo 1) {
        val candidate = formatBigDecimalWithSignificantDigits(value, sigDigits)
        if (measureWidth(candidate) <= availableWidthPx) return candidate
    }

    for (sigDigits in 8 downTo 1) {
        val candidate = formatScientific(value, sigDigits)
        if (measureWidth(candidate) <= availableWidthPx) return candidate
    }

    return formatScientific(value, 1)
}

private fun formatBigDecimalWithSignificantDigits(value: BigDecimal, significantDigits: Int): String {
    val rounded = value.round(MathContext(significantDigits, RoundingMode.HALF_UP))
    return rounded.stripTrailingZeros().toPlainString()
}

private fun formatScientific(value: BigDecimal, significantDigits: Int): String {
    val fractionDigits = (significantDigits - 1).coerceAtLeast(0)
    val pattern = if (fractionDigits == 0) "0E0" else "0.${"#".repeat(fractionDigits)}E0"
    val formatter = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
    return formatter.format(value)
}

@Composable
private fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null,
    isOperator: Boolean = false,
    isEquals: Boolean = false,
    onClick: () -> Unit
) {
    val colors = when {
        containerColor != null || contentColor != null -> ButtonDefaults.buttonColors(
            containerColor = containerColor ?: MaterialTheme.colorScheme.primary,
            contentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary
        )
        isEquals -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        isOperator -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        else -> ButtonDefaults.buttonColors(containerColor = Color(0xFFE6E6E6), contentColor = Color.Black)
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        colors = colors
    ) {
        Text(text = text, fontSize = 26.sp)
    }
}

@Preview(showBackground = true)
@Composable
private fun CalculatorPreview() {
    CalculatorTestTheme {
        CalculatorScreen()
    }
}
