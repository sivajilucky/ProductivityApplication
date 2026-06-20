package com.oqlo.lifetracker.ui.planner

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.oqlo.lifetracker.data.planner.TaskEntity
import com.oqlo.lifetracker.data.planner.TaskPriority
import com.oqlo.lifetracker.ui.common.AppViewModel
import com.oqlo.lifetracker.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlannerViewModel(application: Application) : AppViewModel(application) {

    private val today = DateUtils.todayEpochDay()

    val todayTasks: StateFlow<List<TaskEntity>> =
        app.plannerRepository.tasksForDay(today)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            app.plannerRepository.prefillRecurringTasksForDay(today)
        }
    }

    fun addTask(title: String, category: String, priority: TaskPriority, timeSlot: String?) {
        if (title.isBlank()) return
        viewModelScope.launch {
            app.plannerRepository.addTask(
                TaskEntity(
                    title = title,
                    category = category.ifBlank { "General" },
                    priority = priority,
                    dateEpochDay = today,
                    timeSlot = timeSlot?.takeIf { it.isNotBlank() }
                )
            )
        }
    }

    fun toggleDone(task: TaskEntity) {
        viewModelScope.launch { app.plannerRepository.toggleDone(task) }
    }
}
