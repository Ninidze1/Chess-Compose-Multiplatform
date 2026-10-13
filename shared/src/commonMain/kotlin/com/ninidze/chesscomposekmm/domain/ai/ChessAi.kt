package com.ninidze.chesscomposekmm.domain.ai

import com.ninidze.chesscomposekmm.domain.model.AiMove
import com.ninidze.chesscomposekmm.domain.model.ChessBoard

/** Chooses moves for the computer-controlled side. */
interface ChessAi {
    /**
     * Searches for the best move for the side to move on [board].
     * Implementations must not block the calling thread.
     *
     * @return The chosen move, or null if no move could be found (e.g. no legal move exists).
     */
    suspend fun findBestMove(board: ChessBoard): AiMove?
}
