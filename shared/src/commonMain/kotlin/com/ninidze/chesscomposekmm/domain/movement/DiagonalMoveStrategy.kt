package com.ninidze.chesscomposekmm.domain.movement

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.base.MoveStrategy
import com.ninidze.chesscomposekmm.domain.base.getLineMoves
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.Position

class DiagonalMoveStrategy : MoveStrategy {
    private val directions = listOf(
        Position(1, 1),
        Position(-1, -1),
        Position(1, -1),
        Position(-1, 1)
    )

    override fun getMoves(piece: ChessPiece, chessBoard: ChessBoard): List<Position> {
        return directions.flatMap { direction ->
            chessBoard.getLineMoves(piece, direction)
        }
    }
}