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
    val tasksTotal: Int,
    val screenTimeTrend: List<Pair<LocalDate, Float>> = emptyList(),
    val expenseTrend: List<Pair<LocalDate, Float>> = emptyList(),
    val taskCompletionTrend: List<Pair<LocalDate, Float>> = emptyList()
)

class DashboardViewModel(application: Application) : AppViewModel(application) {

    private val today = DateUtils.todayEpochDay()

    val snapshot = combine(
        app.screenTimeRepository.usageBetween(today - 6, today),
        app.financeRepository.allTransactions(),
        app.plannerRepository.tasksBetween(today - 6, today)
    ) { weekUsage: List<AppUsageEntity>, transactions, weekTasks: List<TaskEntity> ->
        val todayTransactions = transactions.filter {
            LocalDate.ofEpochDay(it.timestampMillis / 86_400_000L).toEpochDay() == today
        }
        val todayUsage = weekUsage.filter { it.dateEpochDay == today }
        val todayTasks = weekTasks.filter { it.dateEpochDay == today }
        val todayDate = LocalDate.now()

        val screenTimeTrend = (6 downTo 0).map { offset ->
            val day = todayDate.minusDays(offset.toLong())
            val minutes = weekUsage.filter { it.dateEpochDay == day.toEpochDay() }
                .sumOf { it.usageMillis } / 60000f
            day to minutes
        }
        val expenseTrend = (6 downTo 0).map { offset ->
            val day = todayDate.minusDays(offset.toLong())
            val expense = transactions.filter {
                it.type == TransactionType.DEBIT &&
                    LocalDate.ofEpochDay(it.timestampMillis / 86_400_000L) == day
            }.sumOf { it.amount }.toFloat()
            day to expense
        }
        val taskCompletionTrend = (6 downTo 0).map { offset ->
            val day = todayDate.minusDays(offset.toLong())
            val dayTasks = weekTasks.filter { it.dateEpochDay == day.toEpochDay() }
            val pct = if (dayTasks.isNotEmpty()) dayTasks.count { it.isDone } * 100f / dayTasks.size else 0f
            day to pct
        }

        DashboardSnapshot(
            totalScreenTimeMillis = todayUsage.sumOf { it.usageMillis },
            todayExpense = todayTransactions.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount },
            todayIncome = todayTransactions.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount },
            tasksCompleted = todayTasks.count { it.isDone },
            tasksTotal = todayTasks.size,
            screenTimeTrend = screenTimeTrend,
            expenseTrend = expenseTrend,
            taskCompletionTrend = taskCompletionTrend
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        DashboardSnapshot(0, 0.0, 0.0, 0, 0)
    )
}
