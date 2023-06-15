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
    override val color: PieceColor,
    override val position: Position,
) : ChessPiece(color, position, KingMoveStrategy()) {
    override val type: PieceType = King

    override fun isValidMove(chessBoard: ChessBoard, targetPosition: Position): Boolean {
        val additionalConditions =
            !chessBoard.isPositionUnderAttack(targetPosition, color.opposite())

        return super.isValidMove(chessBoard, targetPosition) && additionalConditions

    }
}


