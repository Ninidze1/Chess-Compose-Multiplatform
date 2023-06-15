package com.ninidze.chesscomposekmm.android.di

import com.ninidze.chesscomposekmm.android.presentation.AndroidChessViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

internal val viewModels = module {
    viewModelOf(::AndroidChessViewModel)
}