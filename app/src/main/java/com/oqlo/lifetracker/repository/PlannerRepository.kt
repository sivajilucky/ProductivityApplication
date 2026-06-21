package com.oqlo.lifetracker.repository

import com.oqlo.lifetracker.data.planner.PlannerDao
import com.oqlo.lifetracker.data.planner.RecurringTaskEntity
import com.oqlo.lifetracker.data.planner.TaskEntity
import kotlinx.coroutines.flow.Flow

class PlannerRepository(private val dao: PlannerDao) {

    fun tasksForDay(day: Long): Flow<List<TaskEntity>> = dao.tasksForDay(day)

    fun tasksBetween(fromDay: Long, toDay: Long): Flow<List<TaskEntity>> = dao.tasksBetween(fromDay, toDay)

    suspend fun addTask(task: TaskEntity): Long {
        val id = dao.insertTask(task)
        val existing = dao.findRecurringByTitle(task.title)
        if (existing != null) {
            dao.upsertRecurringTask(existing.copy(occurrenceCount = existing.occurrenceCount + 1))
        } else {
            dao.upsertRecurringTask(
                RecurringTaskEntity(
                    title = task.title,
                    category = task.category,
                    priority = task.priority,
                    timeSlot = task.timeSlot,
                    occurrenceCount = 1
                )
            )
        }
        return id
    }

    suspend fun toggleDone(task: TaskEntity) {
        dao.updateTask(task.copy(isDone = !task.isDone))
    }

    /** Pre-fills today with templates that have recurred at least twice (a "habit"). */
    suspend fun prefillRecurringTasksForDay(day: Long) {
        val templates = dao.activeRecurringTasks().filter { it.occurrenceCount >= 2 }
        templates.forEach { template ->
            if (dao.countForTemplateOnDay(day, template.id) == 0) {
                dao.insertTask(
                    TaskEntity(
                        title = template.title,
                        category = template.category,
                        priority = template.priority,
                        dateEpochDay = day,
                        timeSlot = template.timeSlot,
                        recurringTemplateId = template.id
                    )
                )
            }
        }
    }

    /** Carries over unfinished tasks from one day to the next. */
    suspend fun carryOverPending(fromDay: Long, toDay: Long) {
        dao.pendingTasksForDay(fromDay).forEach { pending ->
            dao.insertTask(pending.copy(id = 0, dateEpochDay = toDay))
        }
    }
}
