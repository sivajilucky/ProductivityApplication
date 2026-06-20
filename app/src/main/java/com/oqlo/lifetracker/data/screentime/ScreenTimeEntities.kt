package com.oqlo.lifetracker.data.screentime

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppUsageCategory {
    SOCIAL_MEDIA, ENTERTAINMENT, PRODUCTIVE, WORK, UNCATEGORIZED
}

/** One row per app per calendar day; usageMillis accumulates as the day progresses. */
@Entity(tableName = "app_usage", primaryKeys = ["packageName", "dateEpochDay"])
data class AppUsageEntity(
    val packageName: String,
    val dateEpochDay: Long,
    val appLabel: String,
    val usageMillis: Long
)

@Entity(tableName = "app_category", primaryKeys = ["packageName"])
data class AppCategoryEntity(
    val packageName: String,
    val category: AppUsageCategory
)
