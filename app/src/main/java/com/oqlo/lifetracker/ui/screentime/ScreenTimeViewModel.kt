package com.oqlo.lifetracker.ui.screentime

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.oqlo.lifetracker.data.screentime.AppUsageCategory
import com.oqlo.lifetracker.data.screentime.AppUsageEntity
import com.oqlo.lifetracker.ui.common.AppViewModel
import com.oqlo.lifetracker.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UsageRow(val usage: AppUsageEntity, val category: AppUsageCategory)

class ScreenTimeViewModel(application: Application) : AppViewModel(application) {

    private val today = DateUtils.todayEpochDay()

    val weekUsage: StateFlow<List<AppUsageEntity>> =
        app.screenTimeRepository.usageBetween(today - 6, today)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayRows: StateFlow<List<UsageRow>> = combine(
        app.screenTimeRepository.usageForDay(today),
        app.screenTimeRepository.allCategories()
    ) { usage, categories ->
        val categoryMap = categories.associateBy({ it.packageName }, { it.category })
        usage.map { UsageRow(it, categoryMap[it.packageName] ?: AppUsageCategory.UNCATEGORIZED) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCategory(packageName: String, category: AppUsageCategory) {
        viewModelScope.launch {
            app.screenTimeRepository.setCategory(packageName, category)
        }
    }
}
