package com.oqlo.lifetracker.service

import com.oqlo.lifetracker.data.finance.TransactionType

data class ParsedTransaction(
    val amount: Double,
    val type: TransactionType,
    val merchant: String
)

/**
 * Best-effort regex parser for typical Indian bank/UPI transaction notification text,
 * e.g. "Rs.250.00 debited from A/c ...XX12 to SWIGGY on 20-06-26" or
 * "You have received Rs 5000 from John Doe via UPI".
 */
object NotificationParser {

    private val amountRegex = Regex("""(?:rs\.?|inr|₹)\s?([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
    private val debitKeywords = listOf("debited", "spent", "paid", "withdrawn", "purchase of")
    private val creditKeywords = listOf("credited", "received", "deposited")
    private val merchantRegex = Regex("""(?:to|at|via)\s+([A-Za-z0-9 .&_-]{3,30})""", RegexOption.IGNORE_CASE)

    fun parse(title: String, text: String): ParsedTransaction? {
        val combined = "$title $text"
        val lower = combined.lowercase()

        val amountMatch = amountRegex.find(combined) ?: return null
        val amount = amountMatch.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null

        val type = when {
            debitKeywords.any { lower.contains(it) } -> TransactionType.DEBIT
            creditKeywords.any { lower.contains(it) } -> TransactionType.CREDIT
            else -> return null // not a transaction notification we recognize
        }

        val merchant = merchantRegex.find(combined)?.groupValues?.get(1)?.trim()
            ?: title.takeIf { it.isNotBlank() }
            ?: "Unknown"

        return ParsedTransaction(amount, type, merchant)
    }
}
