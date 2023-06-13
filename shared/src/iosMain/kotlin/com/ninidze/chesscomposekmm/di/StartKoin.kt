package com.ninidze.chesscomposekmm.di

import com.ninidze.chesscomposekmm.presentation.CommonViewModel
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.getKoin

fun initKoin(){
    startKoin {
        modules(viewModel, useCase)
    }
}

inline fun <reified T : CommonViewModel> inject(): Lazy<T> {
    return lazy { getKoin().get(T::class) }
}
