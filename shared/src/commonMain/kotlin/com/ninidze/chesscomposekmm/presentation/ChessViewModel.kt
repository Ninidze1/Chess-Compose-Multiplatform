package com.ninidze.chesscomposekmm.presentation

import com.ninidze.chesscomposekmm.domain.model.AiMove
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.movement.ActionType
import com.ninidze.chesscomposekmm.domain.result.Resource
import com.ninidze.chesscomposekmm.domain.sound.SoundPlayer
import com.ninidze.chesscomposekmm.domain.usecase.LoadBoardUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MoveAIUseCase
import com.ninidze.chesscomposekmm.domain.usecase.MovePieceUseCase
import com.ninidze.chesscomposekmm.domain.usecase.SaveBoardUseCase
import com.ninidze.chesscomposekmm.domain.usecase.StartGameUseCase
import com.ninidze.chesscomposekmm.util.Constants.AI_ANIMATION_TIMEOUT_MS
import com.ninidze.chesscomposekmm.util.Constants.AI_MOVE_DELAY_MS
import com.ninidze.chesscomposekmm.util.Constants.AI_MOVE_REJECTED
import com.ninidze.chesscomposekmm.util.Constants.ERROR_CALCULATING_AI_MOVE
import com.ninidze.chesscomposekmm.util.Constants.ERROR_LOADING_BOARD
import com.ninidze.chesscomposekmm.util.Constants.ERROR_SAVING_BOARD
import com.ninidze.chesscomposekmm.util.Constants.UNCAUGHT_VIEW_MODEL_ERROR
import com.ninidze.chesscomposekmm.util.extensions.isEnPassantCapture
import com.ninidze.chesscomposekmm.util.extensions.isKingChecked
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

sealed class ChessBoardEvents {
    /**
     * The user moved the piece standing on [from] to [to]. [promotion] is the piece a pawn
     * reaching the last rank becomes; null means a queen.
     */
    data class OnPieceMove(
        val from: Position,
        val to: Position,
        val promotion: PieceType? = null
    ) : ChessBoardEvents()

    data object OnGameRestart : ChessBoardEvents()

    /** The UI finished animating the pending AI move [from] -> [to]. */
    data class AiMoveFinished(val from: Position, val to: Position) : ChessBoardEvents()
}

/**
 * The board screen's state holder, shared by Android and iOS.
 *
 * @param coroutineScope The scope the ViewModel's work runs in (Android passes its
 * `viewModelScope`). The ViewModel always works in a child of it, so [clear] cancels only
 * its own work. When null, the ViewModel creates its own scope on the main thread.
 */
class ChessViewModel(
    private val movePieceUseCase: MovePieceUseCase,
    private val soundPlayer: SoundPlayer,
    private val calculateBotMoveUseCase: MoveAIUseCase,
    private val startGameUseCase: StartGameUseCase,
    private val saveBoardUseCase: SaveBoardUseCase,
    private val loadBoardUseCase: LoadBoardUseCase,
    coroutineScope: CoroutineScope? = null
) {

    // A failing coroutine is logged instead of crashing the app, and, thanks to the
    // SupervisorJob, does not cancel the ViewModel's other work.
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println(UNCAUGHT_VIEW_MODEL_ERROR + throwable.message)
    }

    private val viewModelScope: CoroutineScope = CoroutineScope(
        (coroutineScope?.coroutineContext ?: Dispatchers.Main.immediate) +
            SupervisorJob(parent = coroutineScope?.coroutineContext?.get(Job)) +
            exceptionHandler
    )

    // The placeholder board is shown (locked) until the saved game is loaded; it plays no sound.
    private val _uiState = MutableStateFlow(
        ChessUiState(
            board = ChessBoard.createInitialChessBoard(),
            isLoading = true,
            humanColor = HUMAN_COLOR
        )
    )
    val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    // The running turn: the human move, then the AI's thinking, animation and move.
    private var turnJob: Job? = null

    // Completed by the UI when the pending AI move has been animated.
    private var aiAnimationDone: CompletableDeferred<Unit>? = null

    // Incremented on every board replacement, so a delayed AI result can tell whether
    // the board it was computed for is still the current one.
    private var boardVersion = 0L

    init { loadBoard() }

    fun onEvent(event: ChessBoardEvents) {
        when (event) {
            is ChessBoardEvents.OnGameRestart -> restartGame()
            is ChessBoardEvents.OnPieceMove -> onHumanMove(event.from, event.to, event.promotion)
            is ChessBoardEvents.AiMoveFinished -> onAiMoveAnimated(event.from, event.to)
        }
    }

    /**
     * Cancels all of the ViewModel's work (the load, the running turn and pending saves).
     * The ViewModel must not be used afterwards.
     */
    fun clear() {
        viewModelScope.cancel()
    }

    private fun loadBoard() {
        loadJob?.cancel()
        cancelTurn()
        _uiState.update { it.copy(isLoading = true) }
        loadJob = viewModelScope.launch {
            val result = loadBoardUseCase()
            // The load can finish without reaching a cancellation point, so make sure this
            // load was not superseded before applying its board.
            currentCoroutineContext().ensureActive()
            // No saved game, an unreadable one, or a storage failure all start a new game.
            val board = when (result) {
                is Resource.Success -> result.data ?: startNewGame()
                is Resource.Failure -> {
                    println(ERROR_LOADING_BOARD + result.message)
                    startNewGame()
                }
            }
            val version = replaceBoard(board, isLoading = false)
            // A game saved while the AI was to move resumes with the AI's move.
            if (isAiTurn(board)) {
                turnJob = viewModelScope.launch { playAiTurn(board, version) }
            }
        }
    }

    private fun restartGame() {
        // The load would overwrite the new game, and the board is locked until it finishes.
        if (_uiState.value.isLoading) return
        cancelTurn()
        val initialBoard = startNewGame()
        replaceBoard(initialBoard)
        viewModelScope.launch { saveBoard(initialBoard) }
    }

    /** Creates the board for a new game and plays the start sound. */
    private fun startNewGame(): ChessBoard {
        soundPlayer.playSound(ActionType.START)
        return startGameUseCase()
    }

    private fun onHumanMove(from: Position, to: Position, promotion: PieceType?) {
        if (_uiState.value.isInputLocked) return
        cancelTurn()
        // UNDISPATCHED applies the human move before this call returns, so the board (and
        // with it the input lock) is updated before the next UI event can be handled.
        turnJob = viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            playTurn(from, to, promotion)
        }
    }

    /**
     * The whole turn as one sequential pipeline: apply the human move, stop if the game
     * is over, otherwise let the AI answer if it is its turn.
     */
    private suspend fun playTurn(from: Position, to: Position, promotion: PieceType?) {
        val board = _uiState.value.board
        if (board.isGameOver || board.playerTurn != HUMAN_COLOR) return
        val piece = board.getPieceAtPosition(from)
            ?.takeIf { it.color == HUMAN_COLOR }
            ?: return

        val result = movePieceUseCase(board, piece, to, promotion ?: PieceType.Queen)
        if (result !is Resource.Success) return
        val movedBoard = result.data
        val version = applyMove(board, from, to, movedBoard)
        saveBoard(movedBoard)
        // The save can finish without reaching a cancellation point, so check it here.
        currentCoroutineContext().ensureActive()

        if (movedBoard.isGameOver) return
        if (isAiTurn(movedBoard)) playAiTurn(movedBoard, version)
    }

    /**
     * Computes the AI's move for [board], publishes it for the UI to animate, waits for
     * the animation and applies it. [version] is the [boardVersion] [board] belongs to.
     */
    private suspend fun playAiTurn(board: ChessBoard, version: Long) {
        if (version != boardVersion || !isAiTurn(board)) return
        _uiState.update { it.copy(isAiThinking = true) }

        delay(AI_MOVE_DELAY_MS)
        if (version != boardVersion) return

        // Suspends while the search runs off the main thread.
        val aiMove = calculateAiMove(board)
        if (version != boardVersion) return
        if (aiMove == null) {
            // Unreachable while the game is ongoing: the side to move has a legal move.
            _uiState.update { it.copy(isAiThinking = false) }
            return
        }

        val animationDone = CompletableDeferred<Unit>()
        aiAnimationDone = animationDone
        _uiState.update { it.copy(aiMove = aiMove) }
        // Apply the move even if no UI is attached to report the end of the animation.
        withTimeoutOrNull(AI_ANIMATION_TIMEOUT_MS) { animationDone.await() }
        aiAnimationDone = null
        if (version != boardVersion) return

        moveAI(aiMove)
    }

    private fun onAiMoveAnimated(from: Position, to: Position) {
        val aiMove = _uiState.value.aiMove ?: return
        if (aiMove.from != from || aiMove.to != to) return
        aiAnimationDone?.complete(Unit)
    }

    /**
     * Asks the AI for a move, falling back to a legal app move if the AI finds none or
     * the app's rules reject it, so the game never stalls on the AI's turn.
     */
    private suspend fun calculateAiMove(board: ChessBoard): AiMove? {
        val engineMove = try {
            calculateBotMoveUseCase(board)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println(ERROR_CALCULATING_AI_MOVE + e.message)
            null
        }
        if (engineMove != null && isLegalAiMove(board, engineMove)) return engineMove

        println(AI_MOVE_REJECTED + engineMove)
        return board.cells.flatten()
            .filterNotNull()
            .filter { it.color == AI_COLOR }
            .firstNotNullOfOrNull { piece ->
                piece.getAvailableMoves(board).firstOrNull()?.let { AiMove(piece.position, it) }
            }
    }

    // Applies the animated AI move after re-validating it against the current board.
    private suspend fun moveAI(aiMove: AiMove) {
        val board = _uiState.value.board
        if (!isLegalAiMove(board, aiMove)) {
            _uiState.update { it.copy(aiMove = null, isAiThinking = false) }
            return
        }
        val piece = board.getPieceAtPosition(aiMove.from) ?: return
        val result = movePieceUseCase(
            chessBoard = board,
            piece = piece,
            targetPosition = aiMove.to,
            promotion = aiMove.promotion ?: PieceType.Queen
        )
        if (result !is Resource.Success) {
            _uiState.update { it.copy(aiMove = null, isAiThinking = false) }
            return
        }
        applyMove(board, aiMove.from, aiMove.to, result.data)
        saveBoard(result.data)
    }

    private fun isLegalAiMove(board: ChessBoard, move: AiMove): Boolean {
        val (from, to) = move
        if (!from.isValidPosition() || !to.isValidPosition()) return false
        if (!isAiTurn(board)) return false
        val piece = board.getPieceAtPosition(from) ?: return false
        return piece.color == AI_COLOR && piece.isValidMove(board, to)
    }

    private fun isAiTurn(board: ChessBoard): Boolean =
        !board.isGameOver && board.playerTurn == AI_COLOR

    /**
     * Makes [updatedBoard], the result of moving [from] -> [to] on [previousBoard], the current
     * board and plays the sound for that move. The domain move has already switched the turn
     * and evaluated the game result.
     */
    private fun applyMove(
        previousBoard: ChessBoard,
        from: Position,
        to: Position,
        updatedBoard: ChessBoard
    ): Long {
        val version = replaceBoard(updatedBoard)
        soundPlayer.playSound(moveSound(previousBoard, from, to, updatedBoard))
        return version
    }

    /** The sound for a move: game end, then check, promotion, capture, or a plain move. */
    private fun moveSound(
        previousBoard: ChessBoard,
        from: Position,
        to: Position,
        updatedBoard: ChessBoard
    ): ActionType {
        val movedPiece = previousBoard.getPieceAtPosition(from)
        val isPromotion = movedPiece?.type == PieceType.Pawn &&
            updatedBoard.getPieceAtPosition(to)?.type != PieceType.Pawn
        val isCapture = previousBoard.getPieceAtPosition(to) != null ||
            (movedPiece != null && previousBoard.isEnPassantCapture(movedPiece, to))
        return when {
            updatedBoard.isGameOver -> ActionType.FINISH
            updatedBoard.isKingChecked(updatedBoard.playerTurn) -> ActionType.MOVE_CHECK
            isPromotion -> ActionType.PROMOTION
            isCapture -> ActionType.CAPTURE
            else -> ActionType.MOVE
        }
    }

    /**
     * Makes [board] the current board and clears any pending AI move.
     * Returns the new [boardVersion].
     */
    private fun replaceBoard(board: ChessBoard, isLoading: Boolean = false): Long {
        boardVersion++
        _uiState.update {
            it.copy(
                board = board,
                aiMove = null,
                isAiThinking = false,
                isLoading = isLoading
            )
        }
        return boardVersion
    }

    private fun cancelTurn() {
        turnJob?.cancel()
        turnJob = null
        aiAnimationDone = null
        _uiState.update { it.copy(aiMove = null, isAiThinking = false) }
    }

    private suspend fun saveBoard(chessBoard: ChessBoard) {
        val result = saveBoardUseCase(chessBoard)
        if (result is Resource.Failure) {
            println(ERROR_SAVING_BOARD + result.message)
        }
    }

    private companion object {
        val HUMAN_COLOR = PieceColor.White
        val AI_COLOR = PieceColor.Black
    }
}
