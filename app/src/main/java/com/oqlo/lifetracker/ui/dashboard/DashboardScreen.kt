package com.oqlo.lifetracker.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.concurrent.TimeUnit

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val snapshot by viewModel.snapshot.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Today's Snapshot", style = MaterialTheme.typography.headlineSmall)

        SnapshotCard(
            title = "Screen Time",
            value = formatMillis(snapshot.totalScreenTimeMillis)
        )
        SnapshotCard(
            title = "Finance",
            value = "Income ₹${"%.0f".format(snapshot.todayIncome)}  /  Expense ₹${"%.0f".format(snapshot.todayExpense)}"
        )
        SnapshotCard(
            title = "Tasks",
            value = "${snapshot.tasksCompleted} / ${snapshot.tasksTotal} completed"
        )
    }
}

@Composable
private fun SnapshotCard(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun formatMillis(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    return "${minutes / 60}h ${minutes % 60}m"
}
