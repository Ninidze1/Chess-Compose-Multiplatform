package com.ninidze.chesscomposekmm.domain.pieces

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.PieceType.Rook
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.movement.LinearMoveStrategy
import com.ninidze.chesscomposekmm.domain.base.MoveStrategy
import com.ninidze.chesscomposekmm.domain.base.ChessPiece

class Rook(
    color: PieceColor,
    position: Position,
    override val moveStrategy: MoveStrategy = LinearMoveStrategy()
): ChessPiece(color, position) {

    override val type: PieceType = Rook

    override fun getUnfilteredMoves(chessBoard: ChessBoard): List<Position> {
        return moveStrategy.getMoves(this, chessBoard)
    }
}