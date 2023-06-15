package com.ninidze.chesscomposekmm.domain.pieces

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceColor.White
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.PieceType.Pawn
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.movement.PawnMoveStrategy

class Pawn(
    override val color: PieceColor,
    override val position: Position,
) : ChessPiece(
    color, position,
    moveStrategy = PawnMoveStrategy()
) {
    override val type: PieceType = Pawn

    override fun getPotentialCaptures(chessBoard: ChessBoard): List<Position> {
        val direction = if (color == White) -1 else 1
        return listOf(
            Position(position.row + direction, position.column + 1),
            Position(position.row + direction, position.column - 1)
        ).filter { it.isValidPosition() }
    }

    override fun movePieceTo(targetPosition: Position): ChessPiece {
        val pawnReachedEnd =
            (color == White && targetPosition.row == 0) ||
            (color == PieceColor.Black && targetPosition.row == 7)
        if (pawnReachedEnd) {
            return Queen(color, targetPosition)
        }
        return Pawn(color, targetPosition)
    }
}
