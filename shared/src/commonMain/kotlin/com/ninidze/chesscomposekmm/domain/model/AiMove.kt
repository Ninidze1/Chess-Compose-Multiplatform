package com.ninidze.chesscomposekmm.domain.model

/**
 * A move chosen by the AI.
 *
 * @property from The square the moving piece stands on.
 * @property to The square the piece moves to.
 * @property promotion The piece a pawn promotes to, or null if the move is not a promotion.
 */
data class AiMove(
    val from: Position,
    val to: Position,
    val promotion: PieceType? = null
)
