package com.ninidze.chesscomposekmm.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(
            chessViewModelModule,
            useCaseModule,
            platformUtilModule,
            soundPlayerModule,
            chessEngineModule,
            localDataStoreModule,
            repositoryModule
        )
    }
}
