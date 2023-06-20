package com.ninidze.chesscomposekmm.android

import android.app.Application
import com.ninidze.chesscomposekmm.android.di.viewModelsModule
import com.ninidze.chesscomposekmm.di.chessEngineModule
import com.ninidze.chesscomposekmm.di.platformUtilModule
import com.ninidze.chesscomposekmm.di.useCasesModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(
                viewModelsModule, useCasesModule,
                platformUtilModule, chessEngineModule
            )
        }
    }
}