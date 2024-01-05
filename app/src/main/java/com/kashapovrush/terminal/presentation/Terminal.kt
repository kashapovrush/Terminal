package com.kashapovrush.terminal.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.kashapovrush.terminal.data.Bar
import kotlin.math.roundToInt

@Composable
fun Terminal(bars: List<Bar>) {

    var terminal by rememberTerminalState(bars = bars)

    val transformableState = TransformableState { zoomChange, panChanged, _ ->
        val visibleBarsCount = (terminal.visibleBarsCount / zoomChange).roundToInt()
            .coerceAtLeast(40)
            .coerceAtMost(bars.size)
        val scrolledBy = (terminal.scrolledBy + panChanged.x)
            .coerceAtLeast(0f)
            .coerceAtMost(bars.size * terminal.barWidth - terminal.terminalWidth)

        terminal = terminal.copy(
            visibleBarsCount = visibleBarsCount,
            scrolledBy = scrolledBy
        )
    }

    Canvas(modifier = Modifier
        .fillMaxSize()
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
        translate (left = terminal.scrolledBy) {
            bars.forEachIndexed {index, bar ->
                val offsetX = size.width - index * terminal.barWidth
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

        bars.firstOrNull()?.let {
            drawPrices(
                min = min,
                pxPerPoint = pxPerPoint,
                lastPrice = it.close
            )
        }

    }
}

private fun DrawScope.drawPrices(
    min: Float,
    pxPerPoint: Float,
    lastPrice: Float
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

    drawLine(
        color = Color.White,
        start = Offset(0f, (size.height - (lastPrice - min) * pxPerPoint)),
        end = Offset(size.width, (size.height - (lastPrice - min) * pxPerPoint)),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(
                4.dp.toPx(), 4.dp.toPx()
            )
        )
    )

    drawLine(
        color = Color.White,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(
                4.dp.toPx(), 4.dp.toPx()
            )
        )
    )
}