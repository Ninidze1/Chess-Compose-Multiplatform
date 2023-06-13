package com.ninidze.chesscomposekmm.android.di

import com.ninidze.chesscomposekmm.android.presentation.AndroidChessViewModel
import com.ninidze.chesscomposekmm.domain.usecase.CheckMateUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MovePieceUseCase
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModels = module {
    viewModelOf(::AndroidChessViewModel)
}

val useCases = module {
    single { CheckMateUseCase() }
    single { MovePieceUseCase() }
}