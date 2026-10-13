package com.ninidze.chesscomposekmm.di

import com.russhwolf.settings.ExperimentalSettingsApi
import org.koin.core.context.startKoin

@ExperimentalSettingsApi
fun initKoin(){
    startKoin {
        modules(
            chessViewModelModule, useCaseModule,
            platformUtilModule, soundPlayerModule, chessEngineModule,
            localDataStoreModule, repositoryModule
        )
    }
}
