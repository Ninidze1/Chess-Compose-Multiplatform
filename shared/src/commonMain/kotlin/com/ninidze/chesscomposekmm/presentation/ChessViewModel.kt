package com.ninidze.chesscomposekmm.presentation

import com.ninidze.chesscomposekmm.data.helper.Resource
import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.movement.ActionType
import com.ninidze.chesscomposekmm.domain.usecase.LoadBoardUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MoveAIUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MovePieceUseCase
import com.ninidze.chesscomposekmm.domain.usecase.SaveBoardUseCase
import com.ninidze.chesscomposekmm.domain.usecase.StartGameUseCase
import com.ninidze.chesscomposekmm.platform.ChessMediaPlayer
import com.ninidze.chesscomposekmm.util.Constants.ERROR_LOADING_BOARD
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
    private val mediaPlayer: ChessMediaPlayer,
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
                _aiMoveSet.value = null
                val initialBoard = startGameUseCase()
                _chessBoardState.value = initialBoard
                saveBoard(initialBoard)
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
            if (_chessBoardState.value.isGameOver) return@launch
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
        _aiMoveSet.value = null
        val board = _chessBoardState.value
        val piece = board.getPieceAtPosition(aiMoveSet.first) ?: return
        val result = movePieceUseCase(board, piece, aiMoveSet.second)
        if (result is Resource.Success) {
            onMoveApplied(result.data)
        }
    }

    private fun movePiece(piece: ChessPiece, targetPosition: Position) {
        viewModelScope.launch {
            val result = movePieceUseCase(_chessBoardState.value, piece, targetPosition)
            if (result is Resource.Success) {
                onMoveApplied(result.data)
                if (!result.data.isGameOver) calculateAiMove()
            } else if (result is Resource.Failure) return@launch
        }
    }

    // The domain move has already switched the turn and evaluated the game result
    private fun onMoveApplied(updatedBoard: ChessBoard) {
        _chessBoardState.value = updatedBoard
        mediaPlayer.playSound(
            if (updatedBoard.isGameOver) ActionType.FINISH else ActionType.MOVE
        )
        saveBoard(updatedBoard)
    }
}
