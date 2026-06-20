package com.oqlo.lifetracker.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.oqlo.lifetracker.LifeTrackerApp
import com.oqlo.lifetracker.data.finance.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Listens to bank/UPI app notifications (GPay, PhonePe, bank apps, etc.) and auto-records
 * parsed transactions on-device. Requires the user to grant Notification Access manually.
 */
class TransactionNotificationListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    // Common Indian banking / UPI apps; extend as needed.
    private val watchedPackages = setOf(
        "com.google.android.apps.nbu.paisa.user", // GPay
        "com.phonepe.app",
        "net.one97.paytm",
        "com.csam.icici.bank.imobile",
        "com.sbi.lotza.hdfcbank",
        "com.snapwork.hdfc"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        if (packageName !in watchedPackages) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return

        val parsed = NotificationParser.parse(title, text) ?: return

        scope.launch {
            val app = applicationContext as LifeTrackerApp
            app.financeRepository.addTransaction(
                TransactionEntity(
                    amount = parsed.amount,
                    type = parsed.type,
                    merchant = parsed.merchant,
                    category = "Uncategorized",
                    timestampMillis = sbn.postTime,
                    source = "notification",
                    rawText = "$title $text"
                )
            )
        }
    }
}
