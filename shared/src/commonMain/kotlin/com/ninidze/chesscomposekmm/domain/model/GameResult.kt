package com.ninidze.chesscomposekmm.domain.model

/**
 * The outcome of a chess game, evaluated for the side to move.
 */
sealed class GameResult {

    /** The side to move still has at least one legal move. */
    object Ongoing : GameResult()

    /** The side to move has no legal moves and its king is in check. [winner] delivered the mate. */
    data class Checkmate(val winner: PieceColor) : GameResult()

    /** The side to move has no legal moves but its king is not in check. */
    object Stalemate : GameResult()

    /** The game is drawn for a reason other than stalemate. */
    data class Draw(val reason: DrawReason) : GameResult()

    val isGameOver: Boolean
        get() = this !is Ongoing
}

enum class DrawReason {
    InsufficientMaterial,
    FiftyMoveRule,
    ThreefoldRepetition
}
