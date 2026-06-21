package com.oqlo.lifetracker.service

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.oqlo.lifetracker.LifeTrackerApp
import com.oqlo.lifetracker.data.screentime.AppUsageEntity
import com.oqlo.lifetracker.util.DateUtils

/**
 * Pulls aggregated per-app foreground usage for today from UsageStatsManager
 * and upserts it into Room. No manual input required; intended to run periodically.
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

        // INTERVAL_DAILY aligns to fixed daily buckets and often returns empty/stale results for a
        // partial "start of today to now" range. INTERVAL_BEST picks the bucket granularity that
        // best fits the requested range without losing data, which is what we want here.
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, start, end)
        if (stats.isNullOrEmpty()) return Result.success()

        val entries = stats
            .filter { it.totalTimeInForeground > 0 }
            .map { stat ->
                AppUsageEntity(
                    packageName = stat.packageName,
                    dateEpochDay = today,
                    appLabel = labelFor(pm, stat.packageName),
                    usageMillis = stat.totalTimeInForeground
                )
            }

        app.database.screenTimeDao().upsertUsageAll(entries)
        return Result.success()
    }

    private fun labelFor(pm: PackageManager, packageName: String): String = try {
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
    }
}
