package com.ninidze.chesscomposekmm.presentation

import com.ninidze.chesscomposekmm.domain.model.AiMove
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor

/**
 * Everything the board screen needs to render one frame.
 *
 * @property board The current board.
 * @property aiMove The AI move that is being animated, or null if none is pending.
 * @property isLoading True until the saved game has been loaded.
 * @property isAiThinking True from the moment the AI's turn starts until its move is applied.
 * @property humanColor The side controlled by the user.
 */
data class ChessUiState(
    val board: ChessBoard,
    val aiMove: AiMove? = null,
    val isLoading: Boolean = false,
    val isAiThinking: Boolean = false,
    val humanColor: PieceColor = PieceColor.White
) {
    /** True when the user must not be able to select or move pieces. */
    val isInputLocked: Boolean
        get() = isLoading ||
            isAiThinking ||
            aiMove != null ||
            board.isGameOver ||
            board.playerTurn != humanColor
}
