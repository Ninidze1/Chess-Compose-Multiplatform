package com.ninidze.chesscomposekmm.domain.base

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.pieces.Bishop
import com.ninidze.chesscomposekmm.domain.pieces.King
import com.ninidze.chesscomposekmm.domain.pieces.Knight
import com.ninidze.chesscomposekmm.domain.pieces.Pawn
import com.ninidze.chesscomposekmm.domain.pieces.Queen
import com.ninidze.chesscomposekmm.domain.pieces.Rook
import com.ninidze.chesscomposekmm.util.extensions.isKingChecked
import com.ninidze.chesscomposekmm.util.extensions.isKingCheckedAfterMove

/**
 * An abstract representation of a chess piece. This class should be extended by specific types of chess pieces.
 *
 * @property color The color of the chess piece.
 * @property position The current position of the chess piece on the chessboard.
 * @property moveStrategy The strategy used to determine the possible moves of the chess piece.
 * Can be null if the piece has a custom move calculation.
 */
abstract class ChessPiece(
    open val color: PieceColor,
    open val position: Position,
    private val moveStrategy: MoveStrategy
) {
    abstract val type: PieceType

    /**
     * Gets the unfiltered moves for this chess piece on a given chessboard.
     * This should be overridden in each specific chess piece class.
     *
     * @param chessBoard The current state of the chessboard.
     * @return A list of positions to which the chess piece can potentially move.
     */
    open fun getUnfilteredMoves(chessBoard: ChessBoard): List<Position> =
        moveStrategy.getMoves(this, chessBoard)


    /**
     * Checks if a given move is valid for this chess piece on a given chessboard.
     * It checks if the move is in the list of unfiltered moves,
     * if the target position contains a king, and if the move would put the king in check.
     *
     * @param chessBoard The current state of the chessboard.
     * @param targetPosition The position to which the piece is trying to move.
     * @return true if the move is valid, false otherwise.
     */
    open fun isValidMove(chessBoard: ChessBoard, targetPosition: Position): Boolean {
        if (targetPosition !in getUnfilteredMoves(chessBoard)) {
            return false
        }
        if (chessBoard.getPieceAtPosition(targetPosition)?.type == PieceType.King) {
            return false
        }
        if (chessBoard.isKingCheckedAfterMove(this, targetPosition)) {
            return false
        }
        if (chessBoard.isKingChecked(color)) {
            return !chessBoard.isKingCheckedAfterMove(this, targetPosition)
        }
        return true
    }

    /**
     * Gets the available moves for this chess piece on a given chessboard. It filters out the moves that are not valid.
     *
     * @param chessBoard The current state of the chessboard.
     * @return A list of valid positions to which the chess piece can move.
     */
    fun getAvailableMoves(chessBoard: ChessBoard): List<Position> {
        return getUnfilteredMoves(chessBoard).filter { isValidMove(chessBoard, it) }
    }

    /**
     * Gets the potential capture moves for this chess piece on a given chessboard.
     * By default, it is same as the unfiltered moves,
     * but can be overridden in specific chess piece classes.
     *
     * @param chessBoard The current state of the chessboard.
     * @return A list of positions where the chess piece can potentially capture an opponent's piece.
     */
    open fun getPotentialCaptures(chessBoard: ChessBoard): List<Position> =
        getUnfilteredMoves(chessBoard)

    /**
     * Moves the chess piece to a new position on the chessboard.
     * It returns a new instance of the same type of chess piece but at the new position.
     *
     * @param targetPosition The position to which the piece is to be moved.
     * @return A new instance of the same type of chess piece but at the new position.
     * @throws RuntimeException If the type of the chess piece is unknown.
     */
    fun movePieceTo(targetPosition: Position) = when (this) {
        is Pawn -> Pawn(color, targetPosition)
        is King -> King(color, targetPosition)
        is Knight -> Knight(color, targetPosition)
        is Rook -> Rook(color, targetPosition)
        is Bishop -> Bishop(color, targetPosition)
        is Queen -> Queen(color, targetPosition)
        else -> error("Unknown chess piece type")
    }
}