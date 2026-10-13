package com.ninidze.chesscomposekmm.android.di

import com.ninidze.chesscomposekmm.android.presentation.AndroidChessViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val viewModelModule = module {
    viewModelOf(::AndroidChessViewModel)
}