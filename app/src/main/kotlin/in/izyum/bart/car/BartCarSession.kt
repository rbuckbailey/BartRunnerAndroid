package `in`.izyum.bart.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import `in`.izyum.bart.BartRunnerApplication

/**
 * Manages the lifecycle of the Android Auto session.
 * Opens SavedRoutePickerScreen as the root screen and manages foreground repository state.
 */
class BartCarSession : Session(), DefaultLifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    override fun onCreateScreen(intent: Intent): Screen {
        return SavedRoutePickerScreen(carContext)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        val app = carContext.applicationContext as? BartRunnerApplication
        app?.transitRepository?.setAppInForeground(false)
    }
}

