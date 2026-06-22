package com.oqlo.lifetracker.ui.screentime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oqlo.lifetracker.data.screentime.AppUsageCategory
import com.oqlo.lifetracker.ui.common.AnimatedTrendChart
import com.oqlo.lifetracker.ui.common.BarData
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@Composable
fun ScreenTimeScreen(viewModel: ScreenTimeViewModel = viewModel()) {
    val todayRows by viewModel.todayRows.collectAsState()
    val weekUsage by viewModel.weekUsage.collectAsState()

    val totalMillis = todayRows.sumOf { it.usage.usageMillis }
    val wastedMillis = todayRows.filter {
        it.category == AppUsageCategory.SOCIAL_MEDIA || it.category == AppUsageCategory.ENTERTAINMENT
    }.sumOf { it.usage.usageMillis }

    val weekBars = remember(weekUsage) {
        (0..6).map { offsetFromToday ->
            val day = LocalDate.now().minusDays((6 - offsetFromToday).toLong())
            val totalForDay = weekUsage.filter { it.dateEpochDay == day.toEpochDay() }
                .sumOf { it.usageMillis }
            BarData(day.dayOfWeek.name.take(3), (totalForDay / 60000f), Color(0xFF6750A4))
        }
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Screen Time", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PhoneAndroid, contentDescription = "Screen Time", tint = Color(0xFF6750A4))
                        Text("Today", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                    }
                    Text("Total: ${formatMillis(totalMillis)}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
                    Text(
                        "Time wasted: ${formatMillis(wastedMillis)} (${if (totalMillis > 0) wastedMillis * 100 / totalMillis else 0}%)",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )
                    if (weekBars.any { it.value > 0f }) {
                        Text("Last 7 days", style = MaterialTheme.typography.labelSmall)
                        AnimatedTrendChart(
                            data = weekBars,
                            barHeight = 70.dp,
                            valueLabel = { "${it.toInt()}m" }
                        )
                    }
                }
            }
        }
        item { Text("Today's breakdown", style = MaterialTheme.typography.titleMedium) }
        items(todayRows) { row ->
            UsageRowItem(row, onCategoryChange = { viewModel.setCategory(row.usage.packageName, it) })
        }
    }
}

@Composable
private fun UsageRowItem(row: UsageRow, onCategoryChange: (AppUsageCategory) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(row.usage.appLabel, style = MaterialTheme.typography.bodyLarge)
                Text(formatMillis(row.usage.usageMillis), style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(row.category.name.replace("_", " "), style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Set category")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    AppUsageCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name.replace("_", " ")) },
                            onClick = {
                                onCategoryChange(category)
                                menuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatMillis(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    return "${minutes / 60}h ${minutes % 60}m"
}
