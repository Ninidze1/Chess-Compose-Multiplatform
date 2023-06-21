package com.ninidze.chesscomposekmm.android

import android.app.Application
import com.ninidze.chesscomposekmm.android.di.viewModelModule
import com.ninidze.chesscomposekmm.di.chessEngineModule
import com.ninidze.chesscomposekmm.di.localDataStoreModule
import com.ninidze.chesscomposekmm.di.platformUtilModule
import com.ninidze.chesscomposekmm.di.repositoryModule
import com.ninidze.chesscomposekmm.di.useCaseModule
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
                viewModelModule, useCaseModule,
                platformUtilModule, chessEngineModule,
                localDataStoreModule, repositoryModule
            )
        }
    }
}