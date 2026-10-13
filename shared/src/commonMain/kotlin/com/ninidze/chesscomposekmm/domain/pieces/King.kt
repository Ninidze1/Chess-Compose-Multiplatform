package com.ninidze.chesscomposekmm.domain.pieces

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.CastlingRights
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.PieceType.King
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.movement.KingMoveStrategy
import com.ninidze.chesscomposekmm.util.extensions.isOccupied
import com.ninidze.chesscomposekmm.util.extensions.isPositionUnderAttack
import com.ninidze.chesscomposekmm.util.extensions.opposite
import kotlin.math.max
import kotlin.math.min

class King(override val color: PieceColor, override val position: Position) :
    ChessPiece(color, position, KingMoveStrategy()) {
    override val type: PieceType = King

    /** The adjacent squares plus any castling move. */
    override fun getUnfilteredMoves(chessBoard: ChessBoard): List<Position> =
        super.getUnfilteredMoves(chessBoard) + getCastlingMoves(chessBoard)

    /**
     * Only the adjacent squares: castling never captures. This must not include castling,
     * because castling generation itself asks which squares are attacked, and the opposing
     * king is one of the attackers.
     */
    override fun getPotentialCaptures(chessBoard: ChessBoard): List<Position> = super.getUnfilteredMoves(chessBoard)

    /**
     * Castling targets (two files towards the rook) that are allowed by the castling rights,
     * have only empty squares between king and rook, and where the king is not in check and
     * does not pass over an attacked square. Landing in check is rejected by the common
     * legality filter, which simulates the move.
     */
    private fun getCastlingMoves(chessBoard: ChessBoard): List<Position> {
        val homeRow = CastlingRights.homeRow(color)
        if (position != Position(homeRow, CastlingRights.KING_COLUMN)) return emptyList()

        val rights = chessBoard.castlingRights
        val kingSideAllowed = rights.canCastle(color, kingSide = true)
        val queenSideAllowed = rights.canCastle(color, kingSide = false)
        if (!kingSideAllowed && !queenSideAllowed) return emptyList()
        if (chessBoard.isPositionUnderAttack(position, color.opposite())) return emptyList()

        return listOfNotNull(
            if (kingSideAllowed) {
                castlingTarget(chessBoard, CastlingRights.KING_SIDE_ROOK_COLUMN)
            } else {
                null
            },
            if (queenSideAllowed) {
                castlingTarget(chessBoard, CastlingRights.QUEEN_SIDE_ROOK_COLUMN)
            } else {
                null
            }
        )
    }

    private fun castlingTarget(chessBoard: ChessBoard, rookColumn: Int): Position? {
        val row = position.row
        val rook = chessBoard.getPieceAtPosition(Position(row, rookColumn))
        if (rook == null || rook.type != PieceType.Rook || rook.color != color) return null

        val firstBetween = min(position.column, rookColumn) + 1
        val lastBetween = max(position.column, rookColumn) - 1
        val anyBetweenOccupied = (firstBetween..lastBetween).any { column ->
            chessBoard.isOccupied(Position(row, column))
        }
        if (anyBetweenOccupied) return null

        val step = if (rookColumn > position.column) 1 else -1
        val passedSquare = Position(row, position.column + step)
        if (chessBoard.isPositionUnderAttack(passedSquare, color.opposite())) return null

        return Position(row, position.column + 2 * step)
    }
}
