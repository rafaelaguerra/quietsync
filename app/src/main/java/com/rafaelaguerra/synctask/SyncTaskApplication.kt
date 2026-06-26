package com.rafaelaguerra.synctask

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.rafaelaguerra.synctask.di.AppContainer

class SyncTaskApplication : Application() {
    val appContainer: AppContainer by lazy {
        AppContainer(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Enable crash + non-fatal collection in release builds only, so local debugging
        // noise doesn't pollute production reports. Crashlytics installs a global uncaught
        // exception handler here, capturing any otherwise-unhandled error in the app.
        val isDebuggable = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !isDebuggable
    }
}
