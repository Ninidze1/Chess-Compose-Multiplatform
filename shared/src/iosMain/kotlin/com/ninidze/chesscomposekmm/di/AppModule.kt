package com.ninidze.chesscomposekmm.di

import com.ninidze.chesscomposekmm.domain.usecase.CheckMateUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MovePieceUseCase
import com.ninidze.chesscomposekmm.presentation.ChessViewModel
import org.koin.dsl.module

val viewModel = module {
    single {
        ChessViewModel(get(), get())
    }
}

val useCase = module {
     single { CheckMateUseCase() }
     single { MovePieceUseCase() }
}