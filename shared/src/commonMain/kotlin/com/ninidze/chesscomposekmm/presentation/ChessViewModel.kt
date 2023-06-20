package com.ninidze.chesscomposekmm.presentation

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor.Black
import com.ninidze.chesscomposekmm.domain.model.PieceColor.White
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.usecase.CheckGameEndUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MoveAIUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MovePieceUseCase
import com.ninidze.chesscomposekmm.domain.usecase.StartGameUseCase
import com.ninidze.chesscomposekmm.util.Constants.FAILED_TO_START_GAME
import com.ninidze.chesscomposekmm.util.extensions.opposite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

sealed class ChessBoardEvents {
    data class OnPieceMove(val piece: ChessPiece, val position: Position) : ChessBoardEvents()
    object OnGameRestart : ChessBoardEvents()
}

interface CommonViewModel

class ChessViewModel(
    private val movePieceUseCase: MovePieceUseCase,
    private val checkGameEndUseCase: CheckGameEndUseCase,
    private val calculateBotMoveUseCase: MoveAIUseCase,
    private val startGameUseCase: StartGameUseCase,
    coroutineScope: CoroutineScope? = null
) : CommonViewModel {

    private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.Main)

    private val _chessBoardState = MutableStateFlow(
        startGameUseCase().getOrNull() ?: error(FAILED_TO_START_GAME)
    )
    val chessBoardState: StateFlow<ChessBoard> = _chessBoardState

    fun onEvent(event: ChessBoardEvents) {
        when (event) {
            is ChessBoardEvents.OnGameRestart -> {
                _chessBoardState.value = startGameUseCase()
                    .getOrNull() ?: error(FAILED_TO_START_GAME)
            }

            is ChessBoardEvents.OnPieceMove -> {
                movePiece(event.piece, event.position)
            }
        }
    }

    private fun moveAI() {
        viewModelScope.launch {
            val result = calculateBotMoveUseCase(_chessBoardState.value)
            delay(1.seconds)
            if (result.isFailure) return@launch
            _chessBoardState.apply {
                value = result.getOrNull() ?: value
            }
            checkGameEnd()
        }
    }

    private fun movePiece(piece: ChessPiece, targetPosition: Position) {
        viewModelScope.launch {
            val result = movePieceUseCase(_chessBoardState.value, piece, targetPosition)
            if (result.isFailure) return@launch
            _chessBoardState.apply {
                value = result.getOrNull() ?: value
            }
            checkGameEnd()
            moveAI()
        }
    }

    private fun switchPlayerTurn(chessBoard: ChessBoard): ChessBoard {
        return chessBoard.copy(
            playerTurn = if (chessBoard.playerTurn == White) {
                Black
            } else {
                White
            }
        )
    }

    private fun checkGameEnd() {
        viewModelScope.launch {
            val currentPlayerColor = _chessBoardState.value.playerTurn
            val gameEnded = checkGameEndUseCase(_chessBoardState.value, currentPlayerColor)
            if (gameEnded) {
                _chessBoardState.apply {
                    value  = value.copy(winner = currentPlayerColor.opposite())
                }
            } else {
                _chessBoardState.apply {
                    value = switchPlayerTurn(value)
                }
            }
        }
    }
}