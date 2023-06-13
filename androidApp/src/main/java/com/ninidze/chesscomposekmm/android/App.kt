package com.ninidze.chesscomposekmm.android

import android.app.Application
import com.ninidze.chesscomposekmm.android.di.useCases
import com.ninidze.chesscomposekmm.android.di.viewModels
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(viewModels, useCases)
        }
    }
}