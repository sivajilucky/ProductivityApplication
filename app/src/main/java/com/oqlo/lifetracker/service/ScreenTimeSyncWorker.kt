package com.oqlo.lifetracker.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.oqlo.lifetracker.LifeTrackerApp
import com.oqlo.lifetracker.data.screentime.AppUsageEntity
import com.oqlo.lifetracker.util.DateUtils

/**
 * Pulls per-app foreground usage for today from UsageStatsManager and upserts it into Room.
 * No manual input required; intended to run periodically.
 */
class ScreenTimeSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as LifeTrackerApp
        val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val pm = applicationContext.packageManager

        val today = DateUtils.todayEpochDay()
        val start = DateUtils.startOfDayMillis(today)
        val end = System.currentTimeMillis()

        // queryUsageStats(INTERVAL_BEST) reports totalTimeInForeground per bucket, and buckets can
        // overlap the requested range in ways that double-count time — it's known to inflate
        // numbers well above what Android's own Digital Wellbeing screen shows. Walking the raw
        // MOVE_TO_FOREGROUND/MOVE_TO_BACKGROUND event stream and summing only the foreground spans
        // that actually fall within [start, end] gives an exact total instead.
        val usageMillisByPackage = mutableMapOf<String, Long>()
        val foregroundSince = mutableMapOf<String, Long>()
        val events = usageStatsManager.queryEvents(start, end)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND ->
                    foregroundSince[event.packageName] = event.timeStamp
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val since = foregroundSince.remove(event.packageName)
                    if (since != null) {
                        val duration = (event.timeStamp - since).coerceAtLeast(0)
                        usageMillisByPackage[event.packageName] =
                            (usageMillisByPackage[event.packageName] ?: 0) + duration
                    }
                }
            }
        }
        // Any app still in the foreground when the query window ends (e.g. this app right now)
        // contributes the time up to `end` rather than being dropped.
        foregroundSince.forEach { (pkg, since) ->
            val duration = (end - since).coerceAtLeast(0)
            usageMillisByPackage[pkg] = (usageMillisByPackage[pkg] ?: 0) + duration
        }

        val entries = usageMillisByPackage
            .filter { it.value > 0 }
            .map { (packageName, millis) ->
                AppUsageEntity(
                    packageName = packageName,
                    dateEpochDay = today,
                    appLabel = labelFor(pm, packageName),
                    usageMillis = millis
                )
            }
        if (entries.isEmpty()) return Result.success()

        app.database.screenTimeDao().upsertUsageAll(entries)
        return Result.success()
    }

    private fun labelFor(pm: PackageManager, packageName: String): String = try {
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
    }
}
