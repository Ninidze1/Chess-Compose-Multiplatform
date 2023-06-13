package com.ninidze.chesscomposekmm.domain.movement

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.base.MoveStrategy
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.Position

/**
 * A composite implementation of the MoveStrategy interface. This class allows for combining multiple
 * move strategies into one.
 *
 * For example, a queen's movement can be represented as a composite of a rook's and a bishop's movement.
 *
 * @property strategies A list of MoveStrategies that this CompositeMoveStrategy is composed of.
 */
class CompositeMoveStrategy(private vararg val strategies: MoveStrategy) : MoveStrategy {
    override fun getMoves(piece: ChessPiece, chessBoard: ChessBoard): List<Position> {
        return strategies.flatMap { it.getMoves(piece, chessBoard) }
    }
}