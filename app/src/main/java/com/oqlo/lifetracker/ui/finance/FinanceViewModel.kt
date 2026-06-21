package com.oqlo.lifetracker.ui.finance

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.oqlo.lifetracker.data.finance.BudgetGoalEntity
import com.oqlo.lifetracker.data.finance.TransactionEntity
import com.oqlo.lifetracker.data.finance.TransactionType
import com.oqlo.lifetracker.ui.common.AppViewModel
import com.oqlo.lifetracker.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class FinanceViewModel(application: Application) : AppViewModel(application) {

    val allTransactions: StateFlow<List<TransactionEntity>> =
        app.financeRepository.allTransactions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uncategorized: StateFlow<List<TransactionEntity>> =
        app.financeRepository.uncategorizedTransactions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _budgetGoal = MutableStateFlow<BudgetGoalEntity?>(null)
    val budgetGoal: StateFlow<BudgetGoalEntity?> = _budgetGoal

    init {
        viewModelScope.launch {
            _budgetGoal.value = app.financeRepository.budgetGoalFor(DateUtils.currentYearMonth())
        }
    }

    fun addManualTransaction(amount: Double, type: TransactionType, merchant: String, category: String) {
        viewModelScope.launch {
            app.financeRepository.addTransaction(
                TransactionEntity(
                    amount = amount,
                    type = type,
                    merchant = merchant,
                    category = category.ifBlank { "Uncategorized" },
                    timestampMillis = System.currentTimeMillis(),
                    source = "manual",
                    isCategoryConfirmed = category.isNotBlank()
                )
            )
        }
    }

    fun confirmCategory(transaction: TransactionEntity, category: String) {
        viewModelScope.launch {
            app.financeRepository.confirmCategory(transaction, category)
        }
    }

    fun setBudgetGoal(monthlyBudget: Double, savingsGoal: Double) {
        viewModelScope.launch {
            val goal = BudgetGoalEntity(DateUtils.currentYearMonth(), savingsGoal, monthlyBudget)
            app.financeRepository.setBudgetGoal(goal)
            _budgetGoal.value = goal
        }
    }

    /** Net expense per day for the last 7 days (today inclusive), oldest first — for trend charts. */
    fun weeklyExpenseTrend(transactions: List<TransactionEntity>): List<Pair<LocalDate, Double>> {
        val today = LocalDate.now()
        return (6 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            val total = transactions.filter {
                it.type == TransactionType.DEBIT &&
                    LocalDate.ofEpochDay(it.timestampMillis / 86_400_000L) == day
            }.sumOf { it.amount }
            day to total
        }
    }

    fun monthSummary(transactions: List<TransactionEntity>, yearMonth: YearMonth): MonthSummary {
        val filtered = transactions.filter {
            val date = LocalDate.ofEpochDay(it.timestampMillis / 86_400_000L)
            YearMonth.from(date) == yearMonth
        }
        val income = filtered.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
        val expense = filtered.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
        val byCategory = filtered.filter { it.type == TransactionType.DEBIT }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
        return MonthSummary(income, expense, byCategory)
    }
}

data class MonthSummary(val income: Double, val expense: Double, val expenseByCategory: Map<String, Double>) {
    val netSavings: Double get() = income - expense
}
