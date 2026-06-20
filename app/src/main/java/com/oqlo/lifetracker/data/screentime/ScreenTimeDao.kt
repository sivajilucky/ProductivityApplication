package com.oqlo.lifetracker.data.screentime

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenTimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUsage(usage: AppUsageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUsageAll(usage: List<AppUsageEntity>)

    @Query("SELECT * FROM app_usage WHERE dateEpochDay = :day ORDER BY usageMillis DESC")
    fun usageForDay(day: Long): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE dateEpochDay BETWEEN :startDay AND :endDay")
    fun usageBetween(startDay: Long, endDay: Long): Flow<List<AppUsageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setCategory(category: AppCategoryEntity)

    @Query("SELECT * FROM app_category")
    fun allCategories(): Flow<List<AppCategoryEntity>>

    @Query("SELECT * FROM app_category WHERE packageName = :packageName LIMIT 1")
    suspend fun categoryFor(packageName: String): AppCategoryEntity?
}
