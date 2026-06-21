package com.oqlo.lifetracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        WorkScheduler.scheduleScreenTimeSync(context)
        WorkScheduler.runImmediateScreenTimeSync(context)
    }
}

object WorkScheduler {
    private const val IMMEDIATE_SYNC_WORK_NAME = "screen_time_sync_immediate"

    fun scheduleScreenTimeSync(context: Context) {
        val request = PeriodicWorkRequestBuilder<ScreenTimeSyncWorker>(30, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "screen_time_sync_periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    // Periodic WorkManager jobs don't fire immediately on first enqueue, so screen time would
    // otherwise show nothing until up to 30 minutes after install/launch. Run once right away.
    fun runImmediateScreenTimeSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<ScreenTimeSyncWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun scheduleDailyReminder(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_reminder_periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
