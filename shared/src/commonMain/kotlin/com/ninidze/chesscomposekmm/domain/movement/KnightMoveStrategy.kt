package com.ninidze.chesscomposekmm.domain.movement

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.base.MoveStrategy
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.Position

class KnightMoveStrategy: MoveStrategy {
    override fun getMoves(piece: ChessPiece, chessBoard: ChessBoard): List<Position> {
        val position = piece.position
        return listOf(
            Position(position.row - 2, position.column - 1),
            Position(position.row - 2, position.column + 1),
            Position(position.row + 2, position.column - 1),
            Position(position.row + 2, position.column + 1),
            Position(position.row - 1, position.column - 2),
            Position(position.row - 1, position.column + 2),
            Position(position.row + 1, position.column - 2),
            Position(position.row + 1, position.column + 2)
        ).filter { it.isValidPosition() }
    }
}