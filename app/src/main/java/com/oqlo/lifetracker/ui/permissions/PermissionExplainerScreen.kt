package com.oqlo.lifetracker.ui.permissions

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun PermissionExplainerScreen(onContinue: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Welcome to LifeTracker", style = MaterialTheme.typography.headlineSmall)
        Text(
            "To automatically track your screen time and finances, LifeTracker needs two " +
                "permissions. All processing happens on your device — nothing is ever uploaded."
        )

        Text("1. Usage Access", style = MaterialTheme.typography.titleMedium)
        Text("Lets LifeTracker measure how long you spend in each app.")
        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }) { Text("Open Usage Access Settings") }

        Text("2. Notification Access", style = MaterialTheme.typography.titleMedium)
        Text("Lets LifeTracker read bank/UPI transaction notifications to auto-log expenses.")
        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }) { Text("Open Notification Access Settings") }

        Button(onClick = onContinue) { Text("I've granted both — Continue") }
    }
}
