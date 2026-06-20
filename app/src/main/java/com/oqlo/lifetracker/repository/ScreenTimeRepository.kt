package com.oqlo.lifetracker.repository

import com.oqlo.lifetracker.data.screentime.AppCategoryEntity
import com.oqlo.lifetracker.data.screentime.AppUsageCategory
import com.oqlo.lifetracker.data.screentime.AppUsageEntity
import com.oqlo.lifetracker.data.screentime.ScreenTimeDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ScreenTimeRepository(private val dao: ScreenTimeDao) {

    fun usageForDay(day: Long): Flow<List<AppUsageEntity>> = dao.usageForDay(day)

    fun usageBetween(startDay: Long, endDay: Long): Flow<List<AppUsageEntity>> =
        dao.usageBetween(startDay, endDay)

    fun allCategories(): Flow<List<AppCategoryEntity>> = dao.allCategories()

    suspend fun setCategory(packageName: String, category: AppUsageCategory) {
        dao.setCategory(AppCategoryEntity(packageName, category))
    }

    suspend fun recordUsage(entries: List<AppUsageEntity>) {
        dao.upsertUsageAll(entries)
    }

    suspend fun categoryFor(packageName: String): AppUsageCategory {
        return dao.categoryFor(packageName)?.category ?: AppUsageCategory.UNCATEGORIZED
    }

    suspend fun snapshotForDay(day: Long): List<AppUsageEntity> = dao.usageForDay(day).first()
}
