package com.oqlo.lifetracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.oqlo.lifetracker.LifeTrackerApp
import com.oqlo.lifetracker.util.DateUtils

/** Reminds the user to log expenses/tasks and review their end-of-day summary. */
class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as LifeTrackerApp
        val today = DateUtils.todayEpochDay()
        app.plannerRepository.prefillRecurringTasksForDay(today)
        app.plannerRepository.carryOverPending(today - 1, today)
        showReminderNotification()
        return Result.success()
    }

    private fun showReminderNotification() {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "Daily Summary", NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("LifeTracker")
            .setContentText("Review today's tasks and expenses.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "daily_reminder"
        private const val NOTIFICATION_ID = 1001
    }
}
