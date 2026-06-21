package com.oqlo.lifetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.oqlo.lifetracker.service.WorkScheduler
import com.oqlo.lifetracker.ui.navigation.LifeTrackerBottomBar
import com.oqlo.lifetracker.ui.navigation.LifeTrackerNavHost
import com.oqlo.lifetracker.ui.navigation.rememberAppNavController
import com.oqlo.lifetracker.ui.permissions.PermissionExplainerScreen
import com.oqlo.lifetracker.ui.theme.LifeTrackerTheme
import com.oqlo.lifetracker.util.PermissionUtils

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_DEEPLINK = "deeplink"
        const val DEEPLINK_ADD_TASK = "add_task"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WorkScheduler.scheduleScreenTimeSync(this)
        WorkScheduler.runImmediateScreenTimeSync(this)
        WorkScheduler.scheduleDailyReminder(this)

        val deepLink = intent.getStringExtra(EXTRA_DEEPLINK)

        setContent {
            LifeTrackerTheme {
                val permissionsGranted = remember {
                    mutableStateOf(
                        PermissionUtils.hasUsageAccess(this) && PermissionUtils.hasNotificationAccess(this)
                    )
                }

                if (!permissionsGranted.value) {
                    PermissionExplainerScreen(
                        onContinue = {
                            permissionsGranted.value =
                                PermissionUtils.hasUsageAccess(this) && PermissionUtils.hasNotificationAccess(this)
                            if (permissionsGranted.value) {
                                WorkScheduler.runImmediateScreenTimeSync(this)
                            }
                        }
                    )
                } else {
                    val navController = rememberAppNavController()
                    Scaffold(
                        bottomBar = { LifeTrackerBottomBar(navController) }
                    ) { innerPadding ->
                        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.padding(innerPadding)) {
                            LifeTrackerNavHost(navController, startDeepLink = deepLink)
                        }
                    }
                }
            }
        }
    }
}
