package com.oqlo.lifetracker.ui.dashboard

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.oqlo.lifetracker.data.finance.TransactionType
import com.oqlo.lifetracker.data.planner.TaskEntity
import com.oqlo.lifetracker.data.screentime.AppUsageEntity
import com.oqlo.lifetracker.ui.common.AppViewModel
import com.oqlo.lifetracker.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class DashboardSnapshot(
    val totalScreenTimeMillis: Long,
    val todayExpense: Double,
    val todayIncome: Double,
    val tasksCompleted: Int,
    val tasksTotal: Int
)

class DashboardViewModel(application: Application) : AppViewModel(application) {

    private val today = DateUtils.todayEpochDay()

    val snapshot = combine(
        app.screenTimeRepository.usageForDay(today),
        app.financeRepository.allTransactions(),
        app.plannerRepository.tasksForDay(today)
    ) { usage: List<AppUsageEntity>, transactions, tasks: List<TaskEntity> ->
        val todayTransactions = transactions.filter {
            LocalDate.ofEpochDay(it.timestampMillis / 86_400_000L).toEpochDay() == today
        }
        DashboardSnapshot(
            totalScreenTimeMillis = usage.sumOf { it.usageMillis },
            todayExpense = todayTransactions.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount },
            todayIncome = todayTransactions.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount },
            tasksCompleted = tasks.count { it.isDone },
            tasksTotal = tasks.size
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        DashboardSnapshot(0, 0.0, 0.0, 0, 0)
    )
}
