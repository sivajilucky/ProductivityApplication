package com.oqlo.lifetracker.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class BarData(val label: String, val value: Float, val color: Color)

/** Lightweight Compose bar chart — avoids pulling in a heavy charting dependency. */
@Composable
fun SimpleBarChart(data: List<BarData>, modifier: Modifier = Modifier, barHeight: androidx.compose.ui.unit.Dp = 140.dp) {
    val maxValue = (data.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)

    Row(
        modifier = modifier.fillMaxWidth().height(barHeight + 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { bar ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .height(barHeight)
                        .width(28.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight * (bar.value / maxValue))
                            .background(bar.color)
                    )
                }
                Text(bar.label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
