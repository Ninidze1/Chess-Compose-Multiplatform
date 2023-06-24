package com.ninidze.chesscomposekmm.presentation

import com.ninidze.chesscomposekmm.data.helper.Resource
import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor.Black
import com.ninidze.chesscomposekmm.domain.model.PieceColor.White
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.usecase.CheckGameEndUseCase
import com.ninidze.chesscomposekmm.domain.usecase.LoadBoardUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MoveAIUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MovePieceUseCase
import com.ninidze.chesscomposekmm.domain.usecase.SaveBoardUseCase
import com.ninidze.chesscomposekmm.domain.usecase.StartGameUseCase
import com.ninidze.chesscomposekmm.util.Constants.ERROR_LOADING_BOARD
import com.ninidze.chesscomposekmm.util.extensions.movePiece
import com.ninidze.chesscomposekmm.util.extensions.opposite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

sealed class ChessBoardEvents {
    data class OnPieceMove(val piece: ChessPiece, val position: Position) : ChessBoardEvents()
    object OnGameRestart : ChessBoardEvents()
    object AiMoveFinished : ChessBoardEvents()
}

class ChessViewModel(
    private val movePieceUseCase: MovePieceUseCase,
    private val checkGameEndUseCase: CheckGameEndUseCase,
    private val calculateBotMoveUseCase: MoveAIUseCase,
    private val startGameUseCase: StartGameUseCase,
    private val saveBoardUseCase: SaveBoardUseCase,
    private val loadBoardUseCase: LoadBoardUseCase,
    coroutineScope: CoroutineScope? = null
) {

    private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.Main)

    private val _chessBoardState = MutableStateFlow(startGameUseCase())
    val chessBoardState: StateFlow<ChessBoard> = _chessBoardState

    private val _aiMoveSet = MutableStateFlow<Pair<Position, Position>?>(null)
    val aiMoveSet: StateFlow<Pair<Position, Position>?> = _aiMoveSet

    init { loadBoard() }

    fun onEvent(event: ChessBoardEvents) {
        when (event) {
            is ChessBoardEvents.OnGameRestart -> {
                _chessBoardState.value = startGameUseCase()
                saveBoard(startGameUseCase())
            }

            is ChessBoardEvents.OnPieceMove -> {
                movePiece(event.piece, event.position)
            }

            is ChessBoardEvents.AiMoveFinished -> {
                moveAI(_aiMoveSet.value ?: return)
            }
        }
    }

    private fun loadBoard() {
        viewModelScope.launch {
            val result = loadBoardUseCase.invoke()
            if (result is Resource.Success) {
                _chessBoardState.value = result.data.first()
            } else if (result is Resource.Failure) {
                println(ERROR_LOADING_BOARD + result.message)
            }
        }
    }

    private fun saveBoard(chessBoard: ChessBoard) {
        viewModelScope.launch {
            val result = saveBoardUseCase(chessBoard)
            if (result is Resource.Success) {
                println("ChessViewModel: Board saved")
            } else if (result is Resource.Failure) {
                return@launch
            }
        }
    }

    // After AI move is calculated, animation starts
    private fun calculateAiMove() {
        viewModelScope.launch {
            val aiMove = calculateBotMoveUseCase(_chessBoardState.value)
            val piece = _chessBoardState.value.getPieceAtPosition(aiMove.first)
            val targetPosition = aiMove.second
            if (piece == null) return@launch
            if (!piece.isValidMove(_chessBoardState.value, targetPosition)) return@launch

            delay(1.seconds)
            _aiMoveSet.apply { value = aiMove }
        }
    }

    // After animation is finished, this function is called, and Board is updated
    private fun moveAI(aiMoveSet: Pair<Position, Position>) {
        val piece = _chessBoardState.value.getPieceAtPosition(aiMoveSet.first) ?: return
        val targetPosition = aiMoveSet.second
        val updatedChessBoard = _chessBoardState.value.movePiece(piece, targetPosition)
        _chessBoardState.apply { value = updatedChessBoard }
        checkGameEnd()
    }

    private fun movePiece(piece: ChessPiece, targetPosition: Position) {
        viewModelScope.launch {
            val result = movePieceUseCase(_chessBoardState.value, piece, targetPosition)
            if (result is Resource.Success) {
                _chessBoardState.apply { value = result.data }
                checkGameEnd()
                calculateAiMove()
            } else if (result is Resource.Failure) return@launch
        }
    }

    private fun switchPlayerTurnAndSave(chessBoard: ChessBoard): ChessBoard {
        val newBoard = chessBoard.copy(
            playerTurn = if (chessBoard.playerTurn == White) {
                Black
            } else {
                White
            }
        )
        saveBoard(newBoard)
        return newBoard
    }

    private fun checkGameEnd() {
        viewModelScope.launch {
            val board = _chessBoardState.value
            val currentPlayerColor = board.playerTurn

            val gameEnded = checkGameEndUseCase(board, currentPlayerColor)
            if (gameEnded) {
                _chessBoardState.apply {
                    value = value.copy(winner = currentPlayerColor.opposite())
                    saveBoard(ChessBoard.createInitialChessBoard())
                }
            } else {
                _chessBoardState.apply {
                    value = switchPlayerTurnAndSave(value)
                }
            }
        }
    }
}