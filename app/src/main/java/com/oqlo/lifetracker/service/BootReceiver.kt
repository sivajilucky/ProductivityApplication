package com.oqlo.lifetracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        WorkScheduler.scheduleScreenTimeSync(context)
    }
}

object WorkScheduler {
    fun scheduleScreenTimeSync(context: Context) {
        val request = PeriodicWorkRequestBuilder<ScreenTimeSyncWorker>(30, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun scheduleDailyReminder(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
