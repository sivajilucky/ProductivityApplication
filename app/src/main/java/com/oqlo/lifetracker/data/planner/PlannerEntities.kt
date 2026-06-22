package com.oqlo.lifetracker.data.planner

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority { LOW, MEDIUM, HIGH }

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val priority: TaskPriority,
    val dateEpochDay: Long,
    val timeSlot: String? = null, // "HH:mm" optional
    val isDone: Boolean = false,
    val recurringTemplateId: Long? = null,
    // Only meaningful when timeSlot is set — schedules an exact-time system notification.
    val reminderEnabled: Boolean = false
)

/** A recurring template (e.g. "Gym at 6 AM") auto-prefilled each morning. */
@Entity(tableName = "recurring_tasks")
data class RecurringTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val priority: TaskPriority,
    val timeSlot: String? = null,
    val occurrenceCount: Int = 1,
    val isActive: Boolean = true
)
