package com.kashapovrush.terminal.presentation

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kashapovrush.terminal.data.Bar
import java.util.Calendar
import kotlin.math.roundToInt

@OptIn(ExperimentalTextApi::class)
@Composable
fun Terminal() {

    val viewModel: MainViewModel = viewModel()
    val screenState = viewModel.state.collectAsState()
    when (val currentState = screenState.value) {
        is TerminalScreenState.Content -> {
            var terminal by rememberTerminalState(bars = currentState.results)

            val transformableState = rememberTransformableState { zoomChange, panChanged, _ ->
                val visibleBarsCount = (terminal.visibleBarsCount / zoomChange).roundToInt()
                    .coerceAtLeast(40)
                    .coerceAtMost(currentState.results.size)
                val scrolledBy = (terminal.scrolledBy + panChanged.x)
                    .coerceAtLeast(0f)
                    .coerceAtMost(currentState.results.size * terminal.barWidth - terminal.terminalWidth)

                terminal = terminal.copy(
                    visibleBarsCount = visibleBarsCount,
                    scrolledBy = scrolledBy
                )
            }

            val textMeasurer = rememberTextMeasurer()

            Canvas(modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .background(Color.Black)
                .padding(
                    top = 32.dp,
                    bottom = 32.dp
                )
                .transformable(transformableState)
                .onSizeChanged {
                    terminal = terminal.copy(terminalWidth = it.width.toFloat())
                }) {
                val max = terminal.visibleBars.maxOf { it.high }
                val min = terminal.visibleBars.minOf { it.low }
                val pxPerPoint = size.height / (max - min)
                translate(left = terminal.scrolledBy) {
                    currentState.results.forEachIndexed { index, bar ->
                        val offsetX = size.width - index * terminal.barWidth
                        drawTimeDelimiter(
                            bar = bar,
                            nextBar = if (index < currentState.results.size - 1) {
                                currentState.results[index + 1]
                            } else {
                                null
                            },
                            timeFrame = currentState.timeFrame,
                            offsetX = offsetX,
                            textMeasurer = textMeasurer
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(offsetX, size.height - ((bar.low - min) * pxPerPoint)),
                            end = Offset(offsetX, size.height - ((bar.high - min) * pxPerPoint)),
                            strokeWidth = 1f
                        )

                        drawLine(
                            color = if (bar.open < bar.close) Color.Green else Color.Red,
                            start = Offset(offsetX, size.height - ((bar.open - min) * pxPerPoint)),
                            end = Offset(offsetX, size.height - ((bar.close - min) * pxPerPoint)),
                            strokeWidth = terminal.barWidth / 2
                        )
                    }
                }

                currentState.results.firstOrNull()?.let {
                    drawPrices(
                        max = max,
                        min = min,
                        pxPerPoint = pxPerPoint,
                        lastPrice = it.close,
                        textMeasurer = textMeasurer
                    )
                }

            }
            TimeFrames(selected = currentState.timeFrame, onSelectedTimeFrame = {
                viewModel.loadBars(it)
            })
        }

        is TerminalScreenState.Initial -> {

        }

        is TerminalScreenState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        else -> {}
    }


}

@OptIn(ExperimentalTextApi::class)
fun DrawScope.drawTimeDelimiter(
    bar: Bar,
    nextBar: Bar?,
    timeFrame: TimeFrame,
    offsetX: Float,
    textMeasurer: TextMeasurer
) {
    val calendar = bar.calendar

    val minutes = calendar.get(Calendar.MINUTE)
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val shouldDrawTimeDelimiters = when (timeFrame) {
        TimeFrame.MIN_5 -> {
            minutes == 0
        }

        TimeFrame.MIN_15 -> {
            minutes == 0 && hours % 2 == 0
        }

        TimeFrame.MIN_30, TimeFrame.HOUR_1 -> {
            val nextDay = nextBar?.calendar?.get(Calendar.DAY_OF_MONTH)
            day != nextDay
        }
    }

    if (!shouldDrawTimeDelimiters) return

    drawLine(
        color = Color.White.copy(alpha = 0.5f),
        start = Offset(offsetX, 0f),
        end = Offset(offsetX, size.height),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(4.dp.toPx(), 4.dp.toPx())
        )
    )

    val month = calendar.get(Calendar.MONTH)
    val text = when (timeFrame) {
        TimeFrame.MIN_5, TimeFrame.MIN_15 -> {
            String.format("%02d:00", hours)
        }

        TimeFrame.MIN_30, TimeFrame.HOUR_1 -> {
            String.format("%02d.%02d", day, (month + 1))
        }
    }

    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = Color.White,
            fontSize = 12.sp
        )
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(offsetX - textLayoutResult.size.width / 2, size.height)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeFrames(
    selected: TimeFrame,
    onSelectedTimeFrame: (TimeFrame) -> Unit
) {
    Row(
        modifier = Modifier
            .wrapContentSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TimeFrame.values().forEach {
            val textTimeFrame = when (it) {
                TimeFrame.MIN_5 -> "M5"
                TimeFrame.MIN_15 -> "M15"
                TimeFrame.MIN_30 -> "M30"
                TimeFrame.HOUR_1 -> "H1"
            }
            val isSelected = selected == it

            AssistChip(
                onClick = { onSelectedTimeFrame(it) },
                label = { Text(text = textTimeFrame) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected) Color.White else Color.Black,
                    labelColor = if (isSelected) Color.Black else Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawPrices(
    max: Float,
    min: Float,
    pxPerPoint: Float,
    lastPrice: Float,
    textMeasurer: TextMeasurer
) {
    drawLine(
        color = Color.White,
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(
                4.dp.toPx(), 4.dp.toPx()
            )
        )
    )

    drawTextPrice(
        textMeasurer = textMeasurer,
        price = max.toString(),
        offsetY = 0f
    )

    val lastPriceOffsetY = size.height - (lastPrice - min) * pxPerPoint
    drawLine(
        color = Color.White,
        start = Offset(0f, lastPriceOffsetY),
        end = Offset(size.width, lastPriceOffsetY),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(
                4.dp.toPx(), 4.dp.toPx()
            )
        )
    )

    drawTextPrice(
        textMeasurer = textMeasurer,
        price = max.toString(),
        offsetY = lastPriceOffsetY
    )

    val minPriceOffsetY = size.height

    drawLine(
        color = Color.White,
        start = Offset(0f, minPriceOffsetY),
        end = Offset(size.width, minPriceOffsetY),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(
                4.dp.toPx(), 4.dp.toPx()
            )
        )
    )

    drawTextPrice(
        textMeasurer = textMeasurer,
        price = lastPrice.toString(),
        offsetY = minPriceOffsetY
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawTextPrice(
    textMeasurer: TextMeasurer,
    price: String,
    offsetY: Float
) {
    val textLayoutResult = textMeasurer.measure(
        text = price,
        style = TextStyle(
            color = Color.White,
            fontSize = 12.sp
        )
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(size.width - textLayoutResult.size.width, offsetY)
    )
}