package com.ninidze.chesscomposekmm.presentation

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor.Black
import com.ninidze.chesscomposekmm.domain.model.PieceColor.White
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.usecase.CheckMateUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MovePieceUseCase
import com.ninidze.chesscomposekmm.util.extensions.opposite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ChessBoardEvents {
    data class OnPieceMove(val piece: ChessPiece, val position: Position) : ChessBoardEvents()
    object OnGameRestart : ChessBoardEvents()
}

interface CommonViewModel

class ChessViewModel (
    private val movePieceUseCase: MovePieceUseCase,
    private val checkmateUseCase: CheckMateUseCase,
    coroutineScope: CoroutineScope? = null
): CommonViewModel {
    private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.Main)

    private val _chessBoardState = MutableStateFlow(ChessBoard.createInitialChessBoard())
    val chessBoardState: StateFlow<ChessBoard> = _chessBoardState

    fun onEvent(event: ChessBoardEvents) {
        when (event) {
            ChessBoardEvents.OnGameRestart -> {
                _chessBoardState.value = ChessBoard.createInitialChessBoard()
            }
            is ChessBoardEvents.OnPieceMove -> {
                movePiece(event.piece, event.position)
            }
        }
    }

    private fun movePiece(piece: ChessPiece, targetPosition: Position) {
        viewModelScope.launch {
            val result = movePieceUseCase(_chessBoardState.value, piece, targetPosition)
            if (result.isSuccess) {
                _chessBoardState.value = result.getOrNull() ?: _chessBoardState.value
                _chessBoardState.value = switchPlayerTurn(_chessBoardState.value)
                checkForCheckmate()
            } else {
                println("Invalid move")
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

    private fun checkForCheckmate() {
        val currentPlayerColor = _chessBoardState.value.playerTurn
        val isCheckmate = checkmateUseCase(_chessBoardState.value, currentPlayerColor)
        if (isCheckmate) {
            _chessBoardState.value =
                _chessBoardState.value.copy(winner = currentPlayerColor.opposite())
        }
    }
}