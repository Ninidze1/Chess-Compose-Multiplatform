package com.ninidze.chesscomposekmm.domain.movement

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.base.MoveStrategy
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor.White
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.util.extensions.isOccupied
import com.ninidze.chesscomposekmm.util.extensions.isOccupiedByOpponent

class PawnMoveStrategy : MoveStrategy {

    override fun getMoves(piece: ChessPiece, chessBoard: ChessBoard): List<Position> {
        val position = piece.position
        val color = piece.color

        val direction = if (color == White) -1 else 1

        val forwardOne = Position(position.row + direction, position.column)
        val forwardTwo = Position(position.row + direction * 2, position.column)
        val forwardLeft = Position(position.row + direction, position.column - 1)
        val forwardRight = Position(position.row + direction, position.column + 1)

        val possibleMoves = mutableListOf<Position>()
        val isOnStartingPosition =
            (position.row == 6 && color == White) || (position.row == 1 && color != White)

        if (forwardOne.isValidPosition() && !chessBoard.isOccupied(forwardOne)) {
            possibleMoves.add(forwardOne)

            if (isOnStartingPosition && !chessBoard.isOccupied(forwardTwo)) {
                possibleMoves.add(forwardTwo)
            }
        }

        if (forwardLeft.isValidPosition() && chessBoard.isOccupiedByOpponent(forwardLeft, color)) {
            possibleMoves.add(forwardLeft)
        }

        if (forwardRight.isValidPosition() && chessBoard.isOccupiedByOpponent(forwardRight, color)) {
            possibleMoves.add(forwardRight)
        }

        return possibleMoves
    }
}