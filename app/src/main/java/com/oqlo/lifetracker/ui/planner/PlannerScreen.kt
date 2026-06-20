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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oqlo.lifetracker.data.planner.TaskEntity
import com.oqlo.lifetracker.data.planner.TaskPriority

@Composable
fun PlannerScreen(viewModel: PlannerViewModel = viewModel(), openAddTaskOnStart: Boolean = false) {
    val tasks by viewModel.todayTasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(openAddTaskOnStart) }

    LaunchedEffect(openAddTaskOnStart) {
        if (openAddTaskOnStart) showAddDialog = true
    }

    val completed = tasks.count { it.isDone }
    val completionPct = if (tasks.isNotEmpty()) completed * 100 / tasks.size else 0

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text("Today's Plan", style = MaterialTheme.typography.headlineSmall)
                Text("$completed of ${tasks.size} done ($completionPct%)")
            }
            items(tasks) { task ->
                TaskRow(task, onToggle = { viewModel.toggleDone(task) })
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, category, priority, timeSlot ->
                viewModel.addTask(title, category, priority, timeSlot)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TaskRow(task: TaskEntity, onToggle: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row {
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

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, TaskPriority, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var timeSlot by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }

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
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, category, priority, timeSlot) }) { Text("Save") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}
