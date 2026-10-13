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
import com.ninidze.chesscomposekmm.util.Constants.UNKNOWN_CHESS_PIECE
import com.ninidze.chesscomposekmm.util.extensions.isKingCheckedAfterMove
import com.ninidze.chesscomposekmm.util.extensions.isOccupiedByAlly

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
    open fun getUnfilteredMoves(chessBoard: ChessBoard): List<Position> = moveStrategy.getMoves(this, chessBoard)

    /**
     * Checks if a given move is legal for this chess piece on a given chessboard:
     * it must be one of the unfiltered moves, and pass [isLegalTarget].
     *
     * @param chessBoard The current state of the chessboard.
     * @param targetPosition The position to which the piece is trying to move.
     * @return true if the move is valid, false otherwise.
     */
    fun isValidMove(chessBoard: ChessBoard, targetPosition: Position): Boolean =
        targetPosition in getUnfilteredMoves(chessBoard) && isLegalTarget(chessBoard, targetPosition)

    /**
     * Gets the available moves for this chess piece on a given chessboard. It filters out the moves that are not valid.
     *
     * @param chessBoard The current state of the chessboard.
     * @return A list of valid positions to which the chess piece can move.
     */
    fun getAvailableMoves(chessBoard: ChessBoard): List<Position> =
        getUnfilteredMoves(chessBoard).filter { isLegalTarget(chessBoard, it) }

    /**
     * Filters one of the unfiltered moves: the target must not hold a king or an ally, and
     * the move must not leave the own king in check. The move is simulated with all its side
     * effects (castling rook, en-passant capture), so this applies to every move type.
     */
    private fun isLegalTarget(chessBoard: ChessBoard, targetPosition: Position): Boolean {
        if (chessBoard.getPieceAtPosition(targetPosition)?.type == PieceType.King) {
            return false
        }
        if (chessBoard.isOccupiedByAlly(targetPosition, color)) {
            return false
        }
        return !chessBoard.isKingCheckedAfterMove(this, targetPosition)
    }

    /**
     * Gets the potential capture moves for this chess piece on a given chessboard.
     * By default, it is same as the unfiltered moves,
     * but can be overridden in specific chess piece classes.
     *
     * @param chessBoard The current state of the chessboard.
     * @return A list of positions where the chess piece can potentially capture an opponent's piece.
     */
    open fun getPotentialCaptures(chessBoard: ChessBoard): List<Position> = getUnfilteredMoves(chessBoard)

    /**
     * Moves the chess piece to a new position on the chessboard.
     * It returns a new instance of the same type of chess piece but at the new position.
     *
     * @param targetPosition The position to which the piece is to be moved.
     * @param promotion The piece a pawn becomes when it reaches the last rank.
     * Ignored by every other piece and by pawn moves that do not promote.
     * @return A new instance of the same type of chess piece but at the new position.
     * @throws RuntimeException If the type of the chess piece is unknown.
     */
    open fun movePieceTo(targetPosition: Position, promotion: PieceType = PieceType.Queen): ChessPiece = when (this) {
        is Pawn -> Pawn(color, targetPosition)
        is King -> King(color, targetPosition)
        is Knight -> Knight(color, targetPosition)
        is Rook -> Rook(color, targetPosition)
        is Bishop -> Bishop(color, targetPosition)
        is Queen -> Queen(color, targetPosition)
        else -> error(UNKNOWN_CHESS_PIECE)
    }
}
