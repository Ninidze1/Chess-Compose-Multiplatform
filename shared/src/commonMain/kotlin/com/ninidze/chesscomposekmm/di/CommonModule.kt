package com.ninidze.chesscomposekmm.di

import com.ninidze.chesscomposekmm.data.ai.EngineChessAi
import com.ninidze.chesscomposekmm.data.repository.ChessGameRepository
import com.ninidze.chesscomposekmm.domain.ai.ChessAi
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

// Transposition table size in MB; must be a power of two. The engine default (64 MB) is
// far more than a short mobile search needs.
private const val ENGINE_TRANSPOSITION_TABLE_SIZE_MB = 8

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
    single { CheckGameEndUseCase() }
    single { MovePieceUseCase(get()) }
    single { MoveAIUseCase(get()) }
    single { SaveBoardUseCase(get()) }
    single { LoadBoardUseCase(get()) }
}

val chessEngineModule = module {
    single { FenConverter() }
    // The engine itself is created lazily by EngineChessAi, off the main thread, on the
    // first AI turn.
    single<ChessAi> {
        EngineChessAi(
            fenConverter = get(),
            engineFactory = {
                SearchEngine(
                    Config().apply {
                        transpositionTableSize = ENGINE_TRANSPOSITION_TABLE_SIZE_MB
                    }
                )
            }
        )
    }
}

inline fun <reified T> inject(): Lazy<T> {
    return lazy { KoinPlatform.getKoin().get(T::class) }
}
