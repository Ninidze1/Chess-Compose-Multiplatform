package com.ninidze.chesscomposekmm.di

import org.koin.core.context.startKoin

fun initKoin(){
    startKoin {
        modules(
            viewModel, useCases,
            platformUtil, chessEngineModule
        )
    }
}
