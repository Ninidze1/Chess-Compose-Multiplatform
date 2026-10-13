package com.ninidze.chesscomposekmm.util.extensions

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.CastlingRights
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.util.Constants.KING_NOT_FOUND
import kotlin.math.abs

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
 * it performs no legality checks and changes only [ChessBoard.cells] (no turn, castling
 * rights, en-passant target or clocks), because it is also used to simulate hypothetical
 * moves. Real moves go through MovePieceUseCase.
 *
 * It does carry out the side effects of special moves, so simulations see them too:
 * castling also moves the rook, and an en-passant capture removes the captured pawn.
 *
 * [promotion] is the piece a pawn becomes when it reaches the last rank.
 */
fun ChessBoard.movePiece(
    piece: ChessPiece,
    targetPosition: Position,
    promotion: PieceType = PieceType.Queen
): ChessBoard {
    val currentPosition = piece.position
    val pieceToMove = getPieceAtPosition(currentPosition) ?: piece

    val edits = mutableMapOf<Position, ChessPiece?>()
    edits[currentPosition] = null
    edits[targetPosition] = pieceToMove.movePieceTo(targetPosition, promotion)

    if (isCastlingMove(pieceToMove, targetPosition)) {
        val kingSide = targetPosition.column > currentPosition.column
        val rookFrom = Position(
            currentPosition.row,
            if (kingSide) CastlingRights.KING_SIDE_ROOK_COLUMN else CastlingRights.QUEEN_SIDE_ROOK_COLUMN
        )
        // The rook lands on the square the king passed over.
        val rookTo = Position(currentPosition.row, (currentPosition.column + targetPosition.column) / 2)
        getPieceAtPosition(rookFrom)?.let { rook ->
            edits[rookFrom] = null
            edits[rookTo] = rook.movePieceTo(rookTo)
        }
    }
    if (isEnPassantCapture(pieceToMove, targetPosition)) {
        edits[Position(currentPosition.row, targetPosition.column)] = null
    }

    val updatedCells = cells.mapIndexed { rowId, row ->
        row.mapIndexed { columnId, currentPiece ->
            val position = Position(rowId, columnId)
            if (position in edits) edits[position] else currentPiece
        }
    }

    return copy(cells = updatedCells)
}

/** True if moving [piece] to [targetPosition] is castling: a king stepping two files. */
fun isCastlingMove(piece: ChessPiece, targetPosition: Position): Boolean =
    piece.type == PieceType.King &&
        piece.position.row == targetPosition.row &&
        abs(targetPosition.column - piece.position.column) == 2

/**
 * True if moving [piece] to [targetPosition] is an en-passant capture: a pawn moving
 * diagonally onto the empty [ChessBoard.enPassantTarget], next to an opposing pawn.
 */
fun ChessBoard.isEnPassantCapture(piece: ChessPiece, targetPosition: Position): Boolean {
    if (piece.type != PieceType.Pawn) return false
    if (targetPosition != enPassantTarget) return false
    if (piece.position.column == targetPosition.column) return false
    if (!targetPosition.isValidPosition() || isOccupied(targetPosition)) return false
    val capturedPosition = Position(piece.position.row, targetPosition.column)
    if (!capturedPosition.isValidPosition()) return false
    val captured = getPieceAtPosition(capturedPosition)
    return captured != null && captured.type == PieceType.Pawn && captured.color != piece.color
}

/**
 * Returns the piece on this board that stands on [piece]'s square, provided it has the same
 * color and type. Use this to turn a possibly stale piece reference into the current one.
 */
fun ChessBoard.findPiece(piece: ChessPiece): ChessPiece? {
    return getPieceAtPosition(piece.position)
        ?.takeIf { it.color == piece.color && it.type == piece.type }
}

/** True if any piece of [attackerColor] attacks [targetPosition]. */
fun ChessBoard.isPositionUnderAttack(
    targetPosition: Position,
    attackerColor: PieceColor
): Boolean {
    return cells.any { row ->
        row.any { piece ->
            piece != null &&
                piece.color == attackerColor &&
                targetPosition in piece.getPotentialCaptures(this)
        }
    }
}

fun ChessBoard.isKingChecked(color: PieceColor): Boolean {
    val kingPosition = getKingsPosition(color)
    return this.isPositionUnderAttack(
        targetPosition = kingPosition,
        attackerColor = color.opposite()
    )
}

fun ChessBoard.isKingCheckedAfterMove(piece: ChessPiece, targetPosition: Position): Boolean {
    return movePiece(piece, targetPosition).isKingChecked(color = piece.color)
}

fun ChessBoard.getKingsPosition(color: PieceColor): Position {
    val position = this.cells
        .flatten()
        .find { it?.type == PieceType.King && it.color == color }?.position
    require(position != null) { "$color $KING_NOT_FOUND" }
    return position
}

/**
 * A key that is equal for two boards exactly when they are the same position for the
 * threefold-repetition rule: same pieces on the same squares, same side to move, same
 * castling rights and the same en-passant capture possibilities.
 *
 * The en-passant square only counts when the side to move can actually capture onto it.
 */
fun ChessBoard.positionKey(): String {
    val effectiveEnPassant = enPassantTarget?.takeIf { target ->
        cells.flatten().any { piece ->
            piece != null &&
                piece.type == PieceType.Pawn &&
                piece.color == playerTurn &&
                target in piece.getAvailableMoves(this)
        }
    }
    return buildString {
        cells.forEach { row ->
            row.forEach { piece -> append(piece?.fenChar() ?: '.') }
        }
        append(' ')
        append(if (playerTurn == PieceColor.White) 'w' else 'b')
        append(' ')
        append(castlingRights.toFenField())
        append(' ')
        append(effectiveEnPassant?.toAlgebraic() ?: "-")
    }
}
