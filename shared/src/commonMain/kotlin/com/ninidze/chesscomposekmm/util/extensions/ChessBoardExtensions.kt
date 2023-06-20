package com.ninidze.chesscomposekmm.util.extensions

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.util.Constants.KING_NOT_FOUND
import com.ninidze.chesscomposekmm.util.Constants.PIECE_NOT_FOUND

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

fun ChessBoard.movePiece(piece: ChessPiece, targetPosition: Position): ChessBoard {
    val currentPosition = findPiecePosition(piece)
        ?: error(PIECE_NOT_FOUND)

    val updatedPiece = piece.movePieceTo(targetPosition)

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

fun ChessBoard.findPiecePosition(piece: ChessPiece): Position? {
    cells.forEachIndexed { rowIndex, row ->
        row.forEachIndexed { columnIndex, currentPiece ->
            if (currentPiece == piece) {
                return Position(rowIndex, columnIndex)
            }
        }
    }
    return null
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