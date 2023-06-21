package com.ninidze.chesscomposekmm.di

import com.russhwolf.settings.ExperimentalSettingsApi
import org.koin.core.context.startKoin

@ExperimentalSettingsApi
fun initKoin(){
    startKoin {
        modules(
            viewModel, useCaseModule,
            platformUtilModule, chessEngineModule,
            localDataStoreModule, repositoryModule
        )
    }
}
