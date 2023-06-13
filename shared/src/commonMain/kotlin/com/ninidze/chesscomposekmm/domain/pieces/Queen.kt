package com.ninidze.chesscomposekmm.domain.pieces

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.PieceType.Queen
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.movement.CompositeMoveStrategy
import com.ninidze.chesscomposekmm.domain.movement.DiagonalMoveStrategy
import com.ninidze.chesscomposekmm.domain.movement.LinearMoveStrategy
import com.ninidze.chesscomposekmm.domain.base.MoveStrategy
import com.ninidze.chesscomposekmm.domain.base.ChessPiece

class Queen(
    color: PieceColor,
    position: Position,
    override val moveStrategy: MoveStrategy = CompositeMoveStrategy(
        listOf(
            LinearMoveStrategy(),
            DiagonalMoveStrategy()
        )
    )
): ChessPiece(color, position) {

    override val type: PieceType = Queen

    override fun getUnfilteredMoves(chessBoard: ChessBoard): List<Position> {
        return moveStrategy.getMoves(this, chessBoard)
    }
}