package com.oqlo.lifetracker.data

import androidx.room.TypeConverter
import com.oqlo.lifetracker.data.finance.TransactionType
import com.oqlo.lifetracker.data.planner.TaskPriority
import com.oqlo.lifetracker.data.screentime.AppUsageCategory

class Converters {

    @TypeConverter
    fun fromAppUsageCategory(value: AppUsageCategory): String = value.name

    @TypeConverter
    fun toAppUsageCategory(value: String): AppUsageCategory = AppUsageCategory.valueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)
}
