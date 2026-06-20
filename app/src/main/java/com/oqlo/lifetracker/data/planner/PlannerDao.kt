package com.oqlo.lifetracker.data.planner

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerDao {

    @Insert
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE dateEpochDay = :day ORDER BY timeSlot IS NULL, timeSlot ASC")
    fun tasksForDay(day: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dateEpochDay = :day AND isDone = 0")
    suspend fun pendingTasksForDay(day: Long): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecurringTask(task: RecurringTaskEntity): Long

    @Query("SELECT * FROM recurring_tasks WHERE isActive = 1")
    suspend fun activeRecurringTasks(): List<RecurringTaskEntity>

    @Query("SELECT * FROM recurring_tasks WHERE title = :title LIMIT 1")
    suspend fun findRecurringByTitle(title: String): RecurringTaskEntity?

    @Query("SELECT COUNT(*) FROM tasks WHERE dateEpochDay = :day AND recurringTemplateId = :templateId")
    suspend fun countForTemplateOnDay(day: Long, templateId: Long): Int
}
