package com.ninidze.chesscomposekmm.domain.pieces

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.PieceType.Knight
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.movement.KnightMoveStrategy
import com.ninidze.chesscomposekmm.domain.base.MoveStrategy
import com.ninidze.chesscomposekmm.domain.base.ChessPiece

class Knight(
    color: PieceColor,
    position: Position,
    override val moveStrategy: MoveStrategy = KnightMoveStrategy()
): ChessPiece(color, position) {

    override val type: PieceType = Knight

    override fun getUnfilteredMoves(chessBoard: ChessBoard): List<Position> {
        return moveStrategy.getMoves(this, chessBoard)
    }
}