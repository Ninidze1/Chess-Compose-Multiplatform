package com.ninidze.chesscomposekmm.domain.pieces

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.PieceType.Knight
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.movement.KnightMoveStrategy

class Knight(
    override val color: PieceColor,
    override val position: Position,
) : ChessPiece(
    color, position,
    moveStrategy = KnightMoveStrategy()
) {
    override val type: PieceType = Knight
}