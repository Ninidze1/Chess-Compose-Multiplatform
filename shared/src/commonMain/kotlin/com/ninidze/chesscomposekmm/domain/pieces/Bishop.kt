package com.ninidze.chesscomposekmm.domain.pieces

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.PieceType.Bishop
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.movement.DiagonalMoveStrategy
import com.ninidze.chesscomposekmm.domain.base.MoveStrategy

class Bishop(
    override val color: PieceColor,
    override val position: Position,
) : ChessPiece(
    color, position,
    moveStrategy = DiagonalMoveStrategy()
) {
    override val type: PieceType = Bishop
}