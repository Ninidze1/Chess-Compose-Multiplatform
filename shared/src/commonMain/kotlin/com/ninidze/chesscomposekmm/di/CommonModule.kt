package com.ninidze.chesscomposekmm.di

import com.ninidze.chesscomposekmm.domain.engine.Config
import com.ninidze.chesscomposekmm.domain.engine.search.SearchEngine
import com.ninidze.chesscomposekmm.domain.usecase.CalculateBotMoveUseCase
import com.ninidze.chesscomposekmm.domain.usecase.CheckGameEndUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MovePieceUseCase
import com.ninidze.chesscomposekmm.platform.PlatformUtils
import com.ninidze.chesscomposekmm.util.FenConverter
import org.koin.dsl.module


val platformUtil = module {
    single { PlatformUtils() }
}

val useCases = module {
    single { CheckGameEndUseCase() }
    single { MovePieceUseCase() }
    single { CalculateBotMoveUseCase(get(), get()) }
}

val chessEngineModule = module {
    single { FenConverter() }
    single { SearchEngine(Config()) }
}