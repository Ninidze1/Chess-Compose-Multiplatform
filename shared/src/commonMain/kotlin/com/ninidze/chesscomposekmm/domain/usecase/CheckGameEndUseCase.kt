package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.DrawReason
import com.ninidze.chesscomposekmm.domain.model.GameResult
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.util.extensions.isKingChecked
import com.ninidze.chesscomposekmm.util.extensions.opposite
import com.ninidze.chesscomposekmm.util.extensions.positionKey

/**
 * Evaluates the game result for the side to move ([ChessBoard.playerTurn]).
 * Pure: it reads the board and has no side effects.
 *
 * Checkmate and stalemate take precedence; otherwise the game is drawn by insufficient
 * material, the 50-move rule or threefold repetition (applied automatically, without a claim).
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
            !hasLegalMove && chessBoard.isKingChecked(sideToMove) ->
                GameResult.Checkmate(winner = sideToMove.opposite())
            !hasLegalMove -> GameResult.Stalemate
            hasInsufficientMaterial(chessBoard) -> GameResult.Draw(DrawReason.InsufficientMaterial)
            chessBoard.halfmoveClock >= FIFTY_MOVE_RULE_HALFMOVES -> GameResult.Draw(DrawReason.FiftyMoveRule)
            isThreefoldRepetition(chessBoard) -> GameResult.Draw(DrawReason.ThreefoldRepetition)
            else -> GameResult.Ongoing
        }
    }

    /**
     * True when neither side can ever checkmate: king against king, king and one minor piece
     * against king, or kings with only bishops that all stand on squares of one color
     * (which covers king and bishop against king and bishop with same-colored bishops).
     */
    private fun hasInsufficientMaterial(chessBoard: ChessBoard): Boolean {
        val others = chessBoard.cells.flatten()
            .filterNotNull()
            .filter { it.type != PieceType.King }
        if (others.isEmpty()) return true
        if (others.size == 1 && others.single().type == PieceType.Knight) return true
        if (others.all { it.type == PieceType.Bishop }) {
            val squareColors = others.map { (it.position.row + it.position.column) % 2 }.toSet()
            return squareColors.size == 1
        }
        return false
    }

    private fun isThreefoldRepetition(chessBoard: ChessBoard): Boolean {
        val currentKey = chessBoard.positionKey()
        return chessBoard.positionHistory.count { it == currentKey } >= REPETITION_COUNT
    }

    private companion object {
        const val FIFTY_MOVE_RULE_HALFMOVES = 100
        const val REPETITION_COUNT = 3
    }
}
