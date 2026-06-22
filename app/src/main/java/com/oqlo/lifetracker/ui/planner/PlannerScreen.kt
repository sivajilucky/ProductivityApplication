package com.oqlo.lifetracker.ui.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oqlo.lifetracker.data.planner.TaskEntity
import com.oqlo.lifetracker.data.planner.TaskPriority
import com.oqlo.lifetracker.ui.common.AnimatedTrendChart
import com.oqlo.lifetracker.ui.common.BarData
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PlannerScreen(viewModel: PlannerViewModel = viewModel(), openAddTaskOnStart: Boolean = false) {
    val tasks by viewModel.todayTasks.collectAsState()
    val weekTasks by viewModel.weekTasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(openAddTaskOnStart) }

    LaunchedEffect(openAddTaskOnStart) {
        if (openAddTaskOnStart) showAddDialog = true
    }

    val completed = tasks.count { it.isDone }
    val completionPct = if (tasks.isNotEmpty()) completed * 100 / tasks.size else 0
    val completionTrend = remember(weekTasks) { viewModel.weeklyCompletionTrend(weekTasks) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Today's Plan", style = MaterialTheme.typography.headlineSmall)
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = "Tasks", tint = Color(0xFFEF6C00))
                            Text("Today", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                        }
                        Text(
                            "$completed of ${tasks.size} done ($completionPct%)",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )
                        if (completionTrend.any { it.second > 0f }) {
                            Text("Last 7 days completion", style = MaterialTheme.typography.labelSmall)
                            AnimatedTrendChart(
                                data = completionTrend.map { (day, value) ->
                                    BarData(
                                        day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2),
                                        value,
                                        Color(0xFFEF6C00)
                                    )
                                },
                                barHeight = 70.dp,
                                valueLabel = { "${it.toInt()}%" }
                            )
                        }
                    }
                }
            }
            items(tasks) { task ->
                TaskRow(task, onToggle = { viewModel.toggleDone(task) })
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, category, priority, timeSlot, reminderEnabled ->
                viewModel.addTask(title, category, priority, timeSlot, reminderEnabled)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TaskRow(task: TaskEntity, onToggle: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = task.isDone, onCheckedChange = { onToggle() })
                Column {
                    Text(
                        task.title,
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text("${task.category} • ${task.priority.name}${task.timeSlot?.let { " • $it" } ?: ""}",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, TaskPriority, String?, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var timeSlot by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var reminderEnabled by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add task") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                OutlinedTextField(value = timeSlot, onValueChange = { timeSlot = it }, label = { Text("Time (HH:mm, optional)") })
                Row {
                    TaskPriority.values().forEach { p ->
                        Button(onClick = { priority = p }) { Text(if (priority == p) "● ${p.name}" else p.name) }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                    Text("Remind me")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, category, priority, timeSlot, reminderEnabled) }) { Text("Save") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}
