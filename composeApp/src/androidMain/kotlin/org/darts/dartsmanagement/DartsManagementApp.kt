package org.darts.dartsmanagement

import android.app.Application
import com.google.firebase.FirebaseApp
import org.darts.dartsmanagement.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class DartsManagementApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        initKoin {
            androidLogger()
            androidContext(this@DartsManagementApp)
            /*modules(
                uiModule,
                domainModule,
                dataModule,
                //platformModule
            )*/
        }
    }
}