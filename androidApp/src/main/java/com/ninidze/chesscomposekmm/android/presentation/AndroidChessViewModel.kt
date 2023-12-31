package com.ninidze.chesscomposekmm.android.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.usecase.CheckGameEndUseCase
import com.ninidze.chesscomposekmm.domain.usecase.LoadBoardUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MoveAIUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MovePieceUseCase
import com.ninidze.chesscomposekmm.domain.usecase.SaveBoardUseCase
import com.ninidze.chesscomposekmm.domain.usecase.StartGameUseCase
import com.ninidze.chesscomposekmm.presentation.ChessBoardEvents
import com.ninidze.chesscomposekmm.presentation.ChessViewModel
import kotlinx.coroutines.flow.StateFlow

class AndroidChessViewModel(
    private val movePieceUseCase: MovePieceUseCase,
    private val checkGameEndUseCase: CheckGameEndUseCase,
    private val startGameUseCase: StartGameUseCase,
    private val calculateBotMoveUseCase: MoveAIUseCase,
    private val saveBoardUseCase: SaveBoardUseCase,
    private val loadBoardUseCase: LoadBoardUseCase,
) : ViewModel() {

    private val viewModel by lazy {
        ChessViewModel(
            movePieceUseCase = movePieceUseCase,
            checkGameEndUseCase = checkGameEndUseCase,
            calculateBotMoveUseCase = calculateBotMoveUseCase,
            startGameUseCase = startGameUseCase,
            saveBoardUseCase = saveBoardUseCase,
            loadBoardUseCase = loadBoardUseCase,
            coroutineScope = viewModelScope
        )
    }
    val chessBoardState: StateFlow<ChessBoard> = viewModel.chessBoardState
    val aiMoveSet: StateFlow<Pair<Position, Position>?> = viewModel.aiMoveSet

    fun onEvent(event: ChessBoardEvents) {
        viewModel.onEvent(event)
    }
}