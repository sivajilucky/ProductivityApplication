package com.oqlo.lifetracker

import android.app.Application
import com.oqlo.lifetracker.data.AppDatabase
import com.oqlo.lifetracker.repository.FinanceRepository
import com.oqlo.lifetracker.repository.PlannerRepository
import com.oqlo.lifetracker.repository.ScreenTimeRepository

class LifeTrackerApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val screenTimeRepository: ScreenTimeRepository by lazy { ScreenTimeRepository(database.screenTimeDao()) }
    val financeRepository: FinanceRepository by lazy { FinanceRepository(database.financeDao()) }
    val plannerRepository: PlannerRepository by lazy { PlannerRepository(database.plannerDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
