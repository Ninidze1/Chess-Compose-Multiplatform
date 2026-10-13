package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.GameResult
import com.ninidze.chesscomposekmm.util.extensions.isKingChecked
import com.ninidze.chesscomposekmm.util.extensions.opposite

/**
 * Evaluates the game result for the side to move ([ChessBoard.playerTurn]).
 * Pure: it reads the board and has no side effects.
 */
class CheckGameEndUseCase {
    operator fun invoke(chessBoard: ChessBoard): GameResult {
        val sideToMove = chessBoard.playerTurn
        // getAvailableMoves already drops moves that would leave the own king in check.
        val hasLegalMove = chessBoard.cells.flatten()
            .filterNotNull()
            .filter { it.color == sideToMove }
            .any { it.getAvailableMoves(chessBoard).isNotEmpty() }

        return when {
            hasLegalMove -> GameResult.Ongoing
            chessBoard.isKingChecked(sideToMove) -> GameResult.Checkmate(winner = sideToMove.opposite())
            else -> GameResult.Stalemate
        }
    }
}
