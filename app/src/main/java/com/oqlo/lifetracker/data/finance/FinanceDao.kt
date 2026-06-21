package com.oqlo.lifetracker.data.finance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY timestampMillis DESC")
    fun allTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestampMillis BETWEEN :start AND :end ORDER BY timestampMillis DESC")
    fun transactionsBetween(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE category = 'Uncategorized' ORDER BY timestampMillis DESC")
    fun uncategorizedTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE needsReview = 1 ORDER BY timestampMillis DESC")
    fun needsReviewTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategoryRule(rule: CategoryRuleEntity)

    @Query("SELECT * FROM category_rules")
    suspend fun allCategoryRules(): List<CategoryRuleEntity>

    @Query("SELECT * FROM budget_goal WHERE yearMonth = :yearMonth LIMIT 1")
    suspend fun budgetGoalFor(yearMonth: String): BudgetGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudgetGoal(goal: BudgetGoalEntity)
}
