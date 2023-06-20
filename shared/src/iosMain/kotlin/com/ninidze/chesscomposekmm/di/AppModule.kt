package com.ninidze.chesscomposekmm.di

import com.ninidze.chesscomposekmm.presentation.ChessViewModel
import org.koin.dsl.module

internal val viewModel = module {
    single {
        ChessViewModel(get(), get(), get(), get())
    }
}