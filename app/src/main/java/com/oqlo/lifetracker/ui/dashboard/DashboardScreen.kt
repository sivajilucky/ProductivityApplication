package com.oqlo.lifetracker.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oqlo.lifetracker.ui.common.AnimatedTrendChart
import com.oqlo.lifetracker.ui.common.BarData
import java.time.format.TextStyle
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val snapshot by viewModel.snapshot.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Today's Snapshot", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            SnapshotCard(
                icon = Icons.Filled.PhoneAndroid,
                accent = Color(0xFF6750A4),
                title = "Screen Time",
                value = formatMillis(snapshot.totalScreenTimeMillis),
                trend = snapshot.screenTimeTrend,
                trendColor = Color(0xFF6750A4),
                valueLabel = { "${it.toInt()}m" }
            )
        }
        item {
            SnapshotCard(
                icon = Icons.Filled.AccountBalanceWallet,
                accent = Color(0xFF2E7D32),
                title = "Finance",
                value = "Income ₹${"%.0f".format(snapshot.todayIncome)}  /  Expense ₹${"%.0f".format(snapshot.todayExpense)}",
                trend = snapshot.expenseTrend,
                trendColor = Color(0xFFD32F2F),
                valueLabel = { "₹${it.toInt()}" }
            )
        }
        item {
            SnapshotCard(
                icon = Icons.Filled.CalendarToday,
                accent = Color(0xFFEF6C00),
                title = "Tasks",
                value = "${snapshot.tasksCompleted} / ${snapshot.tasksTotal} completed",
                trend = snapshot.taskCompletionTrend,
                trendColor = Color(0xFFEF6C00),
                valueLabel = { "${it.toInt()}%" }
            )
        }
    }
}

@Composable
private fun SnapshotCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    title: String,
    value: String,
    trend: List<Pair<java.time.LocalDate, Float>>,
    trendColor: Color,
    valueLabel: (Float) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = accent)
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
            }
            Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
            if (trend.any { it.second > 0f }) {
                Text("Last 7 days", style = MaterialTheme.typography.labelSmall)
                AnimatedTrendChart(
                    data = trend.map { (day, value) ->
                        BarData(day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2), value, trendColor)
                    },
                    barHeight = 70.dp,
                    valueLabel = valueLabel
                )
            }
        }
    }
}

private fun formatMillis(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    return "${minutes / 60}h ${minutes % 60}m"
}
