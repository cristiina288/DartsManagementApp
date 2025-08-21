package org.darts.dartsmanagement

import android.app.Application
import org.darts.dartsmanagement.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class DartsManagementApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@DartsManagementApp)
        }
    }
}