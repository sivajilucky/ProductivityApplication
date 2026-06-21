package com.oqlo.lifetracker.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class BarData(val label: String, val value: Float, val color: Color)

/** Lightweight Compose bar chart — avoids pulling in a heavy charting dependency. */
@Composable
fun SimpleBarChart(data: List<BarData>, modifier: Modifier = Modifier, barHeight: androidx.compose.ui.unit.Dp = 140.dp) {
    AnimatedTrendChart(data, modifier, barHeight)
}

/**
 * A gradient-filled, animated bar chart used for "everyday progress" views (screen time, spend,
 * task completion). Each bar eases in from zero height and shows its value above the bar.
 */
@Composable
fun AnimatedTrendChart(
    data: List<BarData>,
    modifier: Modifier = Modifier,
    barHeight: androidx.compose.ui.unit.Dp = 140.dp,
    valueLabel: (Float) -> String = { "%.0f".format(it) }
) {
    val maxValue = (data.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)

    Row(
        modifier = modifier.fillMaxWidth().height(barHeight + 40.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { bar ->
            val targetFraction = (bar.value / maxValue).coerceIn(0f, 1f)
            val animatedFraction by animateFloatAsState(
                targetValue = targetFraction,
                animationSpec = tween(durationMillis = 700),
                label = "barHeight"
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (bar.value > 0f) {
                    Text(
                        valueLabel(bar.value),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .height(barHeight)
                        .width(28.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight * animatedFraction)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(bar.color, bar.color.copy(alpha = 0.45f))
                                )
                            )
                    )
                }
                Text(
                    bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

/**
 * Compact horizontal progress bar with a label and percentage, used for "today vs goal"
 * style summaries on the dashboard.
 */
@Composable
fun LabeledProgressBar(
    label: String,
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "progressFraction"
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("${(animatedFraction * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}
