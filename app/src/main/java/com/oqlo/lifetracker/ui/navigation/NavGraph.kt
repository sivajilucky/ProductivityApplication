package com.oqlo.lifetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.oqlo.lifetracker.ui.dashboard.DashboardScreen
import com.oqlo.lifetracker.ui.finance.FinanceScreen
import com.oqlo.lifetracker.ui.planner.PlannerScreen
import com.oqlo.lifetracker.ui.screentime.ScreenTimeScreen

sealed class Destination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Destination("dashboard", "Dashboard", Icons.Filled.Home)
    object ScreenTime : Destination("screen_time", "Screen Time", Icons.Filled.PhoneAndroid)
    object Finance : Destination("finance", "Finance", Icons.Filled.AccountBalanceWallet)
    object Planner : Destination("planner", "Planner", Icons.Filled.CalendarToday)
}

private val bottomNavItems = listOf(Destination.Dashboard, Destination.ScreenTime, Destination.Finance, Destination.Planner)

@Composable
fun LifeTrackerBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomNavItems.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { androidx.compose.material3.Text(destination.label) }
            )
        }
    }
}

@Composable
fun LifeTrackerNavHost(navController: NavHostController, startDeepLink: String?) {
    NavHost(
        navController = navController,
        startDestination = Destination.Dashboard.route
    ) {
        composable(Destination.Dashboard.route) { DashboardScreen() }
        composable(Destination.ScreenTime.route) { ScreenTimeScreen() }
        composable(Destination.Finance.route) { FinanceScreen() }
        composable(Destination.Planner.route) { PlannerScreen(openAddTaskOnStart = startDeepLink == "add_task") }
    }
}

@Composable
fun rememberAppNavController(): NavHostController = rememberNavController()
