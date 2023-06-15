package com.ninidze.chesscomposekmm.presentation

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor.Black
import com.ninidze.chesscomposekmm.domain.model.PieceColor.White
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.usecase.CalculateBotMoveUseCase
import com.ninidze.chesscomposekmm.domain.usecase.CheckGameEndUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MovePieceUseCase
import com.ninidze.chesscomposekmm.util.Constants.INVALID_MOVE_MESSAGE
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
    private val calculateBotMoveUseCase: CalculateBotMoveUseCase,
    coroutineScope: CoroutineScope? = null
) : CommonViewModel {

    private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.Main)

    private val _chessBoardState = MutableStateFlow(ChessBoard.createInitialChessBoard())
    val chessBoardState: StateFlow<ChessBoard> = _chessBoardState

    fun onEvent(event: ChessBoardEvents) {
        when (event) {
            is ChessBoardEvents.OnGameRestart -> {
                _chessBoardState.value = ChessBoard.createInitialChessBoard()
            }

            is ChessBoardEvents.OnPieceMove -> {
                movePiece(event.piece, event.position)
            }
        }
    }

    private fun moveAI() {
        viewModelScope.launch {
            val result = calculateBotMoveUseCase(_chessBoardState.value)
            delay(0.5.seconds)
            if (result.isSuccess) {
                _chessBoardState.apply {
                    value = result.getOrNull() ?: value
                }
                checkGameEnd()
            } else {
                println(INVALID_MOVE_MESSAGE)
            }
        }
    }

    private fun movePiece(piece: ChessPiece, targetPosition: Position) {
        viewModelScope.launch {
            val result = movePieceUseCase(_chessBoardState.value, piece, targetPosition)
            if (result.isSuccess) {
                _chessBoardState.apply { value = result.getOrNull() ?: value }
                checkGameEnd()
                moveAI()
            } else {
                println(INVALID_MOVE_MESSAGE)
            }
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