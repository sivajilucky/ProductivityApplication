package com.oqlo.lifetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.oqlo.lifetracker.data.finance.BudgetGoalEntity
import com.oqlo.lifetracker.data.finance.CategoryRuleEntity
import com.oqlo.lifetracker.data.finance.FinanceDao
import com.oqlo.lifetracker.data.finance.TransactionEntity
import com.oqlo.lifetracker.data.planner.PlannerDao
import com.oqlo.lifetracker.data.planner.RecurringTaskEntity
import com.oqlo.lifetracker.data.planner.TaskEntity
import com.oqlo.lifetracker.data.screentime.AppCategoryEntity
import com.oqlo.lifetracker.data.screentime.AppUsageEntity
import com.oqlo.lifetracker.data.screentime.ScreenTimeDao

@Database(
    entities = [
        AppUsageEntity::class,
        AppCategoryEntity::class,
        TransactionEntity::class,
        CategoryRuleEntity::class,
        BudgetGoalEntity::class,
        TaskEntity::class,
        RecurringTaskEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun screenTimeDao(): ScreenTimeDao
    abstract fun financeDao(): FinanceDao
    abstract fun plannerDao(): PlannerDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lifetracker.db"
                )
                    // No migrations are authored yet; the app is still pre-release, so a schema
                    // bump just recreates the local cache rather than crashing on open.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
