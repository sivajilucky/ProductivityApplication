package com.oqlo.lifetracker.data.finance

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType { CREDIT, DEBIT }

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val merchant: String,
    val category: String,
    val timestampMillis: Long,
    val source: String, // "notification" | "manual" | "sms"
    val rawText: String? = null,
    val isCategoryConfirmed: Boolean = false
)

/** Learned/keyword based mapping used to auto-categorize future transactions. */
@Entity(tableName = "category_rules")
data class CategoryRuleEntity(
    @PrimaryKey val keyword: String, // lowercase merchant keyword
    val category: String
)

@Entity(tableName = "budget_goal")
data class BudgetGoalEntity(
    @PrimaryKey val yearMonth: String, // e.g. "2026-06"
    val monthlySavingsGoal: Double,
    val monthlyBudget: Double
)
