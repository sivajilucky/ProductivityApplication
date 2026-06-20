package com.oqlo.lifetracker.repository

import com.oqlo.lifetracker.data.finance.BudgetGoalEntity
import com.oqlo.lifetracker.data.finance.CategoryRuleEntity
import com.oqlo.lifetracker.data.finance.FinanceDao
import com.oqlo.lifetracker.data.finance.TransactionEntity
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val dao: FinanceDao) {

    fun allTransactions(): Flow<List<TransactionEntity>> = dao.allTransactions()

    fun transactionsBetween(start: Long, end: Long): Flow<List<TransactionEntity>> =
        dao.transactionsBetween(start, end)

    fun uncategorizedTransactions(): Flow<List<TransactionEntity>> = dao.uncategorizedTransactions()

    /** Looks up a learned category rule by merchant keyword, falling back to "Uncategorized". */
    suspend fun categorize(merchant: String): String {
        val lowerMerchant = merchant.lowercase()
        val rules = dao.allCategoryRules()
        val match = rules.firstOrNull { lowerMerchant.contains(it.keyword) }
        return match?.category ?: DEFAULT_KEYWORD_RULES.entries
            .firstOrNull { (keyword, _) -> lowerMerchant.contains(keyword) }
            ?.value
            ?: "Uncategorized"
    }

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        val category = if (transaction.category.isBlank() || transaction.category == "Uncategorized") {
            categorize(transaction.merchant)
        } else transaction.category
        return dao.insertTransaction(transaction.copy(category = category))
    }

    /** When a user manually fixes a category, learn it for next time. */
    suspend fun confirmCategory(transaction: TransactionEntity, category: String) {
        dao.updateTransaction(transaction.copy(category = category, isCategoryConfirmed = true))
        val keyword = transaction.merchant.lowercase().split(" ").firstOrNull { it.length > 2 }
            ?: transaction.merchant.lowercase()
        dao.upsertCategoryRule(CategoryRuleEntity(keyword, category))
    }

    suspend fun budgetGoalFor(yearMonth: String): BudgetGoalEntity? = dao.budgetGoalFor(yearMonth)

    suspend fun setBudgetGoal(goal: BudgetGoalEntity) = dao.upsertBudgetGoal(goal)

    companion object {
        val DEFAULT_KEYWORD_RULES = mapOf(
            "swiggy" to "Food", "zomato" to "Food", "uber" to "Travel", "ola" to "Travel",
            "rapido" to "Travel", "amazon" to "Shopping", "flipkart" to "Shopping",
            "netflix" to "Entertainment", "spotify" to "Entertainment", "irctc" to "Travel",
            "electricity" to "Utilities", "recharge" to "Utilities", "rent" to "Housing",
            "salary" to "Income", "pharmacy" to "Health", "apollo" to "Health"
        )
    }
}
