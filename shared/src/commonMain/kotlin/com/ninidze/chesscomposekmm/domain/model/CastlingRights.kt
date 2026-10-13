package com.ninidze.chesscomposekmm.domain.model

import com.ninidze.chesscomposekmm.util.Constants.BOARD_SIZE

/**
 * Which castling moves each side may still make. A right is lost for good once the king or
 * the matching rook leaves its home square, or the rook is captured there. A remaining right
 * does not mean castling is legal right now: the squares between must also be empty and the
 * king must not be in, pass through or land in check.
 */
data class CastlingRights(
    val whiteKingSide: Boolean = false,
    val whiteQueenSide: Boolean = false,
    val blackKingSide: Boolean = false,
    val blackQueenSide: Boolean = false
) {
    fun canCastle(color: PieceColor, kingSide: Boolean): Boolean = when (color) {
        PieceColor.White -> if (kingSide) whiteKingSide else whiteQueenSide
        PieceColor.Black -> if (kingSide) blackKingSide else blackQueenSide
    }

    /**
     * Returns these rights without the ones tied to [square]: a king's home square clears
     * both rights of that side, a rook's home corner clears the right on that wing.
     * Call it with both the origin and the destination of every move, so a king or rook
     * that moves, and a rook captured on its corner, all drop the right.
     */
    fun withoutRightsAt(square: Position): CastlingRights {
        val color = when (square.row) {
            homeRow(PieceColor.White) -> PieceColor.White
            homeRow(PieceColor.Black) -> PieceColor.Black
            else -> return this
        }
        val clearKingSide = square.column == KING_COLUMN || square.column == KING_SIDE_ROOK_COLUMN
        val clearQueenSide = square.column == KING_COLUMN || square.column == QUEEN_SIDE_ROOK_COLUMN
        return when (color) {
            PieceColor.White -> copy(
                whiteKingSide = whiteKingSide && !clearKingSide,
                whiteQueenSide = whiteQueenSide && !clearQueenSide
            )
            PieceColor.Black -> copy(
                blackKingSide = blackKingSide && !clearKingSide,
                blackQueenSide = blackQueenSide && !clearQueenSide
            )
        }
    }

    companion object {
        const val KING_COLUMN = 4
        const val KING_SIDE_ROOK_COLUMN = BOARD_SIZE - 1
        const val QUEEN_SIDE_ROOK_COLUMN = 0

        val All = CastlingRights(
            whiteKingSide = true,
            whiteQueenSide = true,
            blackKingSide = true,
            blackQueenSide = true
        )
        val None = CastlingRights()

        /** The row a side's king and rooks start on. Row 0 is rank 8. */
        fun homeRow(color: PieceColor): Int = if (color == PieceColor.White) BOARD_SIZE - 1 else 0
    }
}
