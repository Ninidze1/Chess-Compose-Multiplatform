package com.ninidze.chesscomposekmm.domain.base

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.util.extensions.isOccupiedByAlly
import com.ninidze.chesscomposekmm.util.extensions.isOccupiedByOpponent
import com.ninidze.chesscomposekmm.domain.base.ChessPiece

/**
 * An interface that defines a strategy for movement of a chess piece.
 * This can be implemented by different types of pieces to define their unique movement rules.
 */
interface MoveStrategy {
    fun getMoves(piece: ChessPiece, chessBoard: ChessBoard): List<Position>
}

/**
 * Returns a list of valid line moves (like rook or bishop) for a given piece in a specified direction.
 * This function is a part of the ChessBoard class and can be used to calculate line moves for pieces.
 *
 * @receiver The ChessBoard instance on which the piece is placed.
 * @param piece The chess piece for which to get the moves.
 * @param direction The direction in which to get the moves.
 * @return A list of valid positions to which the piece can move in the given direction.
 */
fun ChessBoard.getLineMoves(piece: ChessPiece, direction: Position): List<Position> {
    val possibleMoves = mutableListOf<Position>()

    var currentPosition = piece.position + direction
    while (currentPosition.isValidPosition() && !this.isOccupiedByAlly(currentPosition, piece.color)) {
        possibleMoves.add(currentPosition)
        if (this.isOccupiedByOpponent(currentPosition, piece.color)) {
            break
        }
        currentPosition += direction
    }
    return possibleMoves
}