package com.ninidze.chesscomposekmm.di

import com.ninidze.chesscomposekmm.data.repository.ChessGameRepository
import com.ninidze.chesscomposekmm.domain.engine.Config
import com.ninidze.chesscomposekmm.domain.engine.search.SearchEngine
import com.ninidze.chesscomposekmm.domain.repository.IChessGameRepository
import com.ninidze.chesscomposekmm.domain.usecase.CheckGameEndUseCase
import com.ninidze.chesscomposekmm.domain.usecase.LoadBoardUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MoveAIUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MovePieceUseCase
import com.ninidze.chesscomposekmm.domain.usecase.SaveBoardUseCase
import com.ninidze.chesscomposekmm.domain.usecase.StartGameUseCase
import com.ninidze.chesscomposekmm.platform.ChessMediaPlayer
import com.ninidze.chesscomposekmm.platform.PlatformUtils
import com.ninidze.chesscomposekmm.util.FenConverter
import org.koin.dsl.module
import org.koin.mp.KoinPlatform


val platformUtilModule = module {
    single { PlatformUtils() }
    single { ChessMediaPlayer() }
}

@Suppress("OPT_IN_USAGE")
val repositoryModule = module {
     single<IChessGameRepository> { ChessGameRepository(get()) }
}

val useCaseModule = module {
    single { StartGameUseCase(get()) }
    single { CheckGameEndUseCase(get()) }
    single { MovePieceUseCase() }
    single { MoveAIUseCase(get(), get()) }
    single { SaveBoardUseCase(get()) }
    single { LoadBoardUseCase(get()) }
}

val chessEngineModule = module {
    single { FenConverter() }
    single { SearchEngine(Config()) }
}

inline fun <reified T> inject(): Lazy<T> {
    return lazy { KoinPlatform.getKoin().get(T::class) }
}