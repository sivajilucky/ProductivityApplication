package com.oqlo.lifetracker.ui.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.oqlo.lifetracker.LifeTrackerApp

/** Base class giving every feature ViewModel direct access to the app's repositories. */
abstract class AppViewModel(application: Application) : AndroidViewModel(application) {
    protected val app: LifeTrackerApp get() = getApplication()
}
