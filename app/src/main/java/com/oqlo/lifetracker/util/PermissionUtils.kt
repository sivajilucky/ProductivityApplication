package com.oqlo.lifetracker.util

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasNotificationAccess(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver, "enabled_notification_listeners"
        ) ?: return false
        return enabledListeners.contains(context.packageName)
    }
}
