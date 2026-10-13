package com.ninidze.chesscomposekmm.util.extensions

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.util.Constants.KING_NOT_FOUND

fun ChessBoard.isOccupied(position: Position): Boolean {
    return cells[position.row][position.column] != null
}

fun ChessBoard.isOccupiedByOpponent(position: Position, color: PieceColor): Boolean {
    val piece = cells[position.row][position.column] ?: return false
    return piece.color != color
}

fun ChessBoard.isOccupiedByAlly(position: Position, color: PieceColor): Boolean {
    val piece = cells[position.row][position.column] ?: return false
    return piece.color == color
}

/**
 * Relocates the piece standing on [piece]'s square to [targetPosition].
 *
 * The piece is resolved from the board by its position, not by reference, so a piece
 * instance taken from an older board never crashes the lookup. This is a raw board edit:
 * it performs no legality checks and does not switch the turn, because it is also used to
 * simulate hypothetical moves. Real moves go through MovePieceUseCase.
 */
fun ChessBoard.movePiece(piece: ChessPiece, targetPosition: Position): ChessBoard {
    val currentPosition = piece.position
    val pieceToMove = getPieceAtPosition(currentPosition) ?: piece

    val updatedPiece = pieceToMove.movePieceTo(targetPosition)

    val updatedCells = cells.mapIndexed { rowId, row ->
        row.mapIndexed { columnId, currentPiece ->
            when {
                Position(rowId, columnId) == currentPosition -> null
                Position(rowId, columnId) == targetPosition -> updatedPiece
                else -> currentPiece
            }
        }
    }

    return copy(cells = updatedCells)
}

/**
 * Returns the piece on this board that stands on [piece]'s square, provided it has the same
 * color and type. Use this to turn a possibly stale piece reference into the current one.
 */
fun ChessBoard.findPiece(piece: ChessPiece): ChessPiece? {
    return getPieceAtPosition(piece.position)
        ?.takeIf { it.color == piece.color && it.type == piece.type }
}

fun ChessBoard.isPositionUnderAttack(
    targetPosition: Position,
    targetColor: PieceColor
): Boolean {
    val pieces = cells
        .flatten()
        .filterNotNull()
        .filter { it.color == targetColor }
    val cellsUnderAttack = pieces.flatMap { it.getPotentialCaptures(this) }
    return pieces.any {
        it.color == targetColor && cellsUnderAttack.contains(targetPosition)
    }
}

fun ChessBoard.isKingChecked(color: PieceColor): Boolean {
    val kingPosition = getKingsPosition(color)
    return this.isPositionUnderAttack(
        targetPosition = kingPosition,
        targetColor = color.opposite()
    )
}

fun ChessBoard.isKingCheckedAfterMove(piece: ChessPiece, targetPosition: Position): Boolean {
    val newChessBoard = this.copy().movePiece(piece, targetPosition)
    return newChessBoard.isKingChecked(color = piece.color)
}

fun ChessBoard.getKingsPosition(color: PieceColor): Position {
    val position = this.cells
        .flatten()
        .find { it?.type == PieceType.King && it.color == color }?.position
    require(position != null) { "$color $KING_NOT_FOUND" }
    return position
}