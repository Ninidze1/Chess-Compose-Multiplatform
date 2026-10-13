package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.domain.ai.ChessAi
import com.ninidze.chesscomposekmm.domain.model.AiMove
import com.ninidze.chesscomposekmm.domain.model.ChessBoard

/**
 * Asks the [ChessAi] for a move for the side to move. The search runs off the calling thread.
 *
 * @return The AI's move, or null if it found none. The move is not validated against the
 * app's rules; the caller must check it before applying it.
 */
class MoveAIUseCase(private val chessAi: ChessAi) {
    suspend operator fun invoke(chessBoard: ChessBoard): AiMove? = chessAi.findBestMove(chessBoard)
}
