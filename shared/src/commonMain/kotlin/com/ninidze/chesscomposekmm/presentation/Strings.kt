package com.ninidze.chesscomposekmm.presentation

import com.ninidze.chesscomposekmm.domain.model.DrawReason
import com.ninidze.chesscomposekmm.domain.model.GameResult
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType

/**
 * All user-facing text of the board screen, in one place until it moves to compose resources.
 */
object Strings {
    const val RESTART = "Restart"
    const val PLAY_AGAIN = "Play again"
    const val VIEW_BOARD = "View board"
    const val YOU_WON = "You won"
    const val YOU_LOST = "You lost"
    const val DRAW = "Draw"
    const val EMPTY_SQUARE = "empty"
    const val SELECTED = "selected"
    const val POSSIBLE_MOVE = "possible move"
    const val POSSIBLE_CAPTURE = "can be captured"
    const val SELECT_PIECE = "select piece"
    const val MOVE_HERE = "move here"
    const val CAPTURE = "capture"
    const val PROMOTE_TO = "Promote to"

    fun colorName(color: PieceColor): String = when (color) {
        PieceColor.White -> "White"
        PieceColor.Black -> "Black"
    }

    fun pieceTypeName(type: PieceType): String = when (type) {
        PieceType.King -> "king"
        PieceType.Queen -> "queen"
        PieceType.Rook -> "rook"
        PieceType.Bishop -> "bishop"
        PieceType.Knight -> "knight"
        PieceType.Pawn -> "pawn"
    }

    /** For example "White knight". */
    fun pieceName(color: PieceColor, type: PieceType): String =
        "${colorName(color)} ${pieceTypeName(type)}"

    /** For example "Promote to knight". */
    fun promoteTo(type: PieceType): String = "$PROMOTE_TO ${pieceTypeName(type)}"

    /** Describes how a finished game ended; null while it is ongoing. */
    fun resultDescription(result: GameResult): String? = when (result) {
        is GameResult.Ongoing -> null
        is GameResult.Checkmate -> "Checkmate: ${colorName(result.winner)} wins"
        is GameResult.Stalemate -> "Draw by stalemate"
        is GameResult.Draw -> when (result.reason) {
            DrawReason.InsufficientMaterial -> "Draw by insufficient material"
            DrawReason.FiftyMoveRule -> "Draw by the 50-move rule"
            DrawReason.ThreefoldRepetition -> "Draw by threefold repetition"
        }
    }

    /** The headline of a finished game from the point of view of [humanColor]; null while ongoing. */
    fun resultTitle(result: GameResult, humanColor: PieceColor): String? = when (result) {
        is GameResult.Ongoing -> null
        is GameResult.Checkmate -> if (result.winner == humanColor) YOU_WON else YOU_LOST
        is GameResult.Stalemate, is GameResult.Draw -> DRAW
    }
}
