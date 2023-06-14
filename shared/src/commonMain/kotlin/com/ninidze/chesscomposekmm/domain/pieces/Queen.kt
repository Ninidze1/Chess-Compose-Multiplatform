package com.ninidze.chesscomposekmm.domain.pieces

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.PieceType.Queen
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.movement.DiagonalMoveStrategy
import com.ninidze.chesscomposekmm.domain.movement.LinearMoveStrategy

class Queen(
    override val color: PieceColor,
    override val position: Position,
) : ChessPiece(
    color, position,
    moveStrategy = LinearMoveStrategy() + DiagonalMoveStrategy()
) {
    override val type: PieceType = Queen
}