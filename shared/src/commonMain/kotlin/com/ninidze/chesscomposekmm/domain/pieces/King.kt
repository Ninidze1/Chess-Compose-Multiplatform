package com.ninidze.chesscomposekmm.domain.pieces

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.PieceType.King
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.movement.KingMoveStrategy
import com.ninidze.chesscomposekmm.util.extensions.isPositionUnderAttack
import com.ninidze.chesscomposekmm.util.extensions.opposite

class King(
    color: PieceColor,
    position: Position,
    override val moveStrategy: KingMoveStrategy = KingMoveStrategy()
) : ChessPiece(color, position) {

    override val type: PieceType = King

    override fun getUnfilteredMoves(chessBoard: ChessBoard): List<Position> {
        return moveStrategy.getMoves(this, chessBoard)
    }

    override fun isValidMove(chessBoard: ChessBoard, targetPosition: Position): Boolean {
        return super.isValidMove(chessBoard, targetPosition) &&
                !chessBoard.isPositionUnderAttack(targetPosition, color.opposite())
    }
}
