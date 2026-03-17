package com.calculator.md3watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.md3watch.ui.theme.WatchCalculatorTheme
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WatchCalculatorTheme {
                CalculatorApp()
            }
        }
    }
}

private const val ErrorText = "\u9519\u8BEF"
private const val MaxInputLength = 14
private val DecimalMathContext = MathContext(12, RoundingMode.HALF_UP)

private enum class CalcOperator(val symbol: String) {
    Add("+"),
    Subtract("\u2212"),
    Multiply("\u00D7"),
    Divide("\u00F7"),
}

private data class CalculatorState(
    val currentInput: String = "0",
    val previousInput: String = "",
    val operator: CalcOperator? = null,
    val waitingForNext: Boolean = false,
    val history: String = "",
)

private sealed interface CalculatorKeyModel {
    data class Digit(val label: String) : CalculatorKeyModel
    data class Action(val label: String, val kind: ActionKind) : CalculatorKeyModel
}

private enum class ActionKind {
    Clear,
    ToggleSign,
    Percent,
    Add,
    Subtract,
    Multiply,
    Divide,
    Sqrt,
    Equals,
}

@Composable
private fun CalculatorApp() {
    var state by remember { mutableStateOf(CalculatorState()) }

    val rows = listOf(
        listOf(
            CalculatorKeyModel.Action("AC", ActionKind.Clear),
            CalculatorKeyModel.Action("\u00B1", ActionKind.ToggleSign),
            CalculatorKeyModel.Action("%", ActionKind.Percent),
            CalculatorKeyModel.Action("\u00F7", ActionKind.Divide),
        ),
        listOf(
            CalculatorKeyModel.Digit("7"),
            CalculatorKeyModel.Digit("8"),
            CalculatorKeyModel.Digit("9"),
            CalculatorKeyModel.Action("\u00D7", ActionKind.Multiply),
        ),
        listOf(
            CalculatorKeyModel.Digit("4"),
            CalculatorKeyModel.Digit("5"),
            CalculatorKeyModel.Digit("6"),
            CalculatorKeyModel.Action("\u2212", ActionKind.Subtract),
        ),
        listOf(
            CalculatorKeyModel.Digit("1"),
            CalculatorKeyModel.Digit("2"),
            CalculatorKeyModel.Digit("3"),
            CalculatorKeyModel.Action("+", ActionKind.Add),
        ),
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val outerPadding = if (maxWidth < 380.dp) 12.dp else 16.dp
        val buttonGap = if (maxWidth < 380.dp) 8.dp else 10.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(outerPadding),
            verticalArrangement = Arrangement.spacedBy(buttonGap),
        ) {
            DisplayPanel(
                state = state,
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxWidth(),
            )

            Column(
                modifier = Modifier
                    .weight(0.62f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(buttonGap),
            ) {
                rows.forEach { row ->
                    KeyRow(
                        keys = row,
                        gap = buttonGap,
                        onKeyPress = { key -> state = handleKeyPress(state, key) },
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonGap),
                ) {
                    CalculatorButton(
                        label = "0",
                        modifier = Modifier.weight(2f),
                        style = ButtonStyle.Digit,
                    ) {
                        state = handleKeyPress(state, CalculatorKeyModel.Digit("0"))
                    }
                    CalculatorButton(
                        label = ".",
                        modifier = Modifier.weight(1f),
                        style = ButtonStyle.Digit,
                    ) {
                        state = handleKeyPress(state, CalculatorKeyModel.Digit("."))
                    }
                    CalculatorButton(
                        label = "\u221A",
                        modifier = Modifier.weight(1f),
                        style = ButtonStyle.Operator,
                    ) {
                        state = handleKeyPress(
                            state,
                            CalculatorKeyModel.Action("\u221A", ActionKind.Sqrt),
                        )
                    }
                    CalculatorButton(
                        label = "=",
                        modifier = Modifier.weight(1f),
                        style = ButtonStyle.Equal,
                    ) {
                        state = handleKeyPress(
                            state,
                            CalculatorKeyModel.Action("=", ActionKind.Equals),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplayPanel(
    state: CalculatorState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.animateContentSize(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = state.history,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = state.currentInput,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun KeyRow(
    keys: List<CalculatorKeyModel>,
    gap: androidx.compose.ui.unit.Dp,
    onKeyPress: (CalculatorKeyModel) -> Unit,
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        keys.forEach { key ->
            val label = when (key) {
                is CalculatorKeyModel.Action -> key.label
                is CalculatorKeyModel.Digit -> key.label
            }

            CalculatorButton(
                label = label,
                modifier = Modifier.weight(1f),
                style = key.toButtonStyle(),
            ) {
                onKeyPress(key)
            }
        }
    }
}

private fun CalculatorKeyModel.toButtonStyle(): ButtonStyle {
    return when (this) {
        is CalculatorKeyModel.Digit -> ButtonStyle.Digit
        is CalculatorKeyModel.Action -> {
            when (kind) {
                ActionKind.Clear,
                ActionKind.ToggleSign,
                ActionKind.Percent -> ButtonStyle.Function
                ActionKind.Equals -> ButtonStyle.Equal
                else -> ButtonStyle.Operator
            }
        }
    }
}

private enum class ButtonStyle {
    Digit,
    Function,
    Operator,
    Equal,
}

@Composable
private fun CalculatorButton(
    label: String,
    modifier: Modifier = Modifier,
    style: ButtonStyle,
    onClick: () -> Unit,
) {
    val containerColor: Color
    val contentColor: Color
    val tonalElevation: androidx.compose.ui.unit.Dp
    val shadowElevation: androidx.compose.ui.unit.Dp

    when (style) {
        ButtonStyle.Digit -> {
            containerColor = MaterialTheme.colorScheme.surfaceVariant
            contentColor = MaterialTheme.colorScheme.onSurface
            tonalElevation = 0.dp
            shadowElevation = 0.dp
        }
        ButtonStyle.Function -> {
            containerColor = MaterialTheme.colorScheme.primaryContainer
            contentColor = MaterialTheme.colorScheme.primary
            tonalElevation = 1.dp
            shadowElevation = 0.dp
        }
        ButtonStyle.Operator -> {
            containerColor = MaterialTheme.colorScheme.primary
            contentColor = MaterialTheme.colorScheme.onPrimary
            tonalElevation = 2.dp
            shadowElevation = 2.dp
        }
        ButtonStyle.Equal -> {
            containerColor = MaterialTheme.colorScheme.secondary
            contentColor = MaterialTheme.colorScheme.onSecondary
            tonalElevation = 3.dp
            shadowElevation = 3.dp
        }
    }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .sizeIn(minHeight = 50.dp),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .padding(PaddingValues(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 21.sp,
                ),
            )
        }
    }
}

private fun handleKeyPress(
    state: CalculatorState,
    key: CalculatorKeyModel,
): CalculatorState {
    return when (key) {
        is CalculatorKeyModel.Digit -> handleDigit(state, key.label)
        is CalculatorKeyModel.Action -> handleAction(state, key.kind)
    }
}

private fun handleDigit(state: CalculatorState, digit: String): CalculatorState {
    if (state.currentInput == ErrorText) {
        return state.copy(
            currentInput = if (digit == ".") "0." else digit,
            previousInput = "",
            operator = null,
            waitingForNext = false,
            history = "",
        )
    }

    if (state.waitingForNext) {
        return state.copy(
            currentInput = if (digit == ".") "0." else digit,
            waitingForNext = false,
        )
    }

    if (digit == ".") {
        if (state.currentInput.contains(".")) {
            return state
        }
        return state.copy(currentInput = "${state.currentInput}.")
    }

    if (state.currentInput.length >= MaxInputLength) {
        return state
    }

    return if (state.currentInput == "0") {
        state.copy(currentInput = digit)
    } else {
        state.copy(currentInput = state.currentInput + digit)
    }
}

private fun handleAction(
    state: CalculatorState,
    action: ActionKind,
): CalculatorState {
    return when (action) {
        ActionKind.Clear -> CalculatorState()
        ActionKind.ToggleSign -> toggleSign(state)
        ActionKind.Percent -> percent(state)
        ActionKind.Add -> setOperator(state, CalcOperator.Add)
        ActionKind.Subtract -> setOperator(state, CalcOperator.Subtract)
        ActionKind.Multiply -> setOperator(state, CalcOperator.Multiply)
        ActionKind.Divide -> setOperator(state, CalcOperator.Divide)
        ActionKind.Sqrt -> sqrtValue(state)
        ActionKind.Equals -> evaluate(state)
    }
}

private fun toggleSign(state: CalculatorState): CalculatorState {
    if (state.currentInput == ErrorText || state.currentInput == "0") {
        return state
    }

    return if (state.currentInput.startsWith("-")) {
        state.copy(currentInput = state.currentInput.removePrefix("-"))
    } else {
        state.copy(currentInput = "-${state.currentInput}")
    }
}

private fun percent(state: CalculatorState): CalculatorState {
    val value = state.currentInput.toBigDecimalOrNull() ?: return CalculatorState(currentInput = ErrorText)
    val result = value.divide(BigDecimal("100"), DecimalMathContext).normalized()
    return state.copy(
        currentInput = result,
        history = "${state.currentInput} %",
        waitingForNext = true,
    )
}

private fun sqrtValue(state: CalculatorState): CalculatorState {
    val value = state.currentInput.toBigDecimalOrNull() ?: return CalculatorState(currentInput = ErrorText)
    if (value < BigDecimal.ZERO) {
        return state.copy(currentInput = ErrorText, history = "\u221A(${state.currentInput})")
    }

    val result = BigDecimal.valueOf(sqrt(value.toDouble())).normalized()
    return state.copy(
        currentInput = result,
        history = "\u221A(${state.currentInput})",
        waitingForNext = true,
    )
}

private fun setOperator(
    state: CalculatorState,
    operator: CalcOperator,
): CalculatorState {
    if (state.currentInput == ErrorText) {
        return CalculatorState()
    }

    if (state.operator != null && !state.waitingForNext) {
        val evaluated = evaluate(state)
        if (evaluated.currentInput == ErrorText) {
            return evaluated
        }
        return evaluated.copy(
            previousInput = evaluated.currentInput,
            operator = operator,
            waitingForNext = true,
            history = "${evaluated.currentInput} ${operator.symbol}",
        )
    }

    return state.copy(
        previousInput = state.currentInput,
        operator = operator,
        waitingForNext = true,
        history = "${state.currentInput} ${operator.symbol}",
    )
}

private fun evaluate(state: CalculatorState): CalculatorState {
    val operator = state.operator ?: return state
    if (state.waitingForNext) {
        return state
    }

    val left = state.previousInput.toBigDecimalOrNull() ?: return CalculatorState(currentInput = ErrorText)
    val right = state.currentInput.toBigDecimalOrNull() ?: return CalculatorState(currentInput = ErrorText)
    val result = runCatching {
        when (operator) {
            CalcOperator.Add -> left.add(right, DecimalMathContext)
            CalcOperator.Subtract -> left.subtract(right, DecimalMathContext)
            CalcOperator.Multiply -> left.multiply(right, DecimalMathContext)
            CalcOperator.Divide -> {
                if (right.compareTo(BigDecimal.ZERO) == 0) {
                    error("divide by zero")
                }
                left.divide(right, DecimalMathContext)
            }
        }.normalized()
    }.getOrElse {
        return state.copy(
            currentInput = ErrorText,
            previousInput = "",
            operator = null,
            waitingForNext = true,
            history = "${state.previousInput} ${operator.symbol} ${state.currentInput}",
        )
    }

    return state.copy(
        currentInput = result,
        previousInput = "",
        operator = null,
        waitingForNext = true,
        history = "${state.previousInput} ${operator.symbol} ${state.currentInput} =",
    )
}

private fun String.toBigDecimalOrNull(): BigDecimal? {
    return runCatching { toBigDecimal() }.getOrNull()
}

private fun BigDecimal.normalized(): String {
    return stripTrailingZeros().toPlainString().let { value ->
        if (value == "-0") {
            "0"
        } else {
            value
        }
    }
}

@Preview(widthDp = 370, heightDp = 430, showBackground = true)
@Composable
private fun CalculatorPreview() {
    WatchCalculatorTheme {
        CalculatorApp()
    }
}
