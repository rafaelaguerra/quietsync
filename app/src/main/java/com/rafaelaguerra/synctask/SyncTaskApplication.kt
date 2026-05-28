package com.rafaelaguerra.synctask

import android.app.Application
import com.rafaelaguerra.synctask.di.AppContainer

class SyncTaskApplication : Application() {
    val appContainer: AppContainer by lazy {
        AppContainer(this)
    }
}
