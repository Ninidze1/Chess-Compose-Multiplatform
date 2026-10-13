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
import com.ninidze.chesscomposekmm.platform.PlatformUtils
import com.ninidze.chesscomposekmm.presentation.ChessViewModel
import com.ninidze.chesscomposekmm.util.FenConverter
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// Transposition table size in MB; must be a power of two. The engine default (64 MB) is
// far more than a short mobile search needs.
private const val ENGINE_TRANSPOSITION_TABLE_SIZE_MB = 8

val platformUtilModule = module {
    // Injected by the engine's own KoinComponent classes.
    singleOf(::PlatformUtils)
}

val repositoryModule = module {
     single<IChessGameRepository> { ChessGameRepository(get()) }
}

val useCaseModule = module {
    singleOf(::StartGameUseCase)
    singleOf(::CheckGameEndUseCase)
    singleOf(::MovePieceUseCase)
    singleOf(::MoveAIUseCase)
    singleOf(::SaveBoardUseCase)
    singleOf(::LoadBoardUseCase)
}

val chessEngineModule = module {
    singleOf(::FenConverter)
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

/**
 * A new [ChessViewModel] per request. The optional parameter is the [CoroutineScope] it runs
 * in (Android passes its `viewModelScope` with `parametersOf`); without one the ViewModel
 * creates and owns its own scope.
 */
val chessViewModelModule = module {
    factory { params ->
        ChessViewModel(
            movePieceUseCase = get(),
            soundPlayer = get(),
            calculateBotMoveUseCase = get(),
            startGameUseCase = get(),
            saveBoardUseCase = get(),
            loadBoardUseCase = get(),
            coroutineScope = params.getOrNull<CoroutineScope>()
        )
    }
}
