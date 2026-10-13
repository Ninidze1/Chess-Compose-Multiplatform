package com.ninidze.chesscomposekmm.util.extensions

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.CastlingRights
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.util.Constants.BOARD_SIZE

/** The FEN letter of this piece: upper case for White, lower case for Black. */
fun ChessPiece.fenChar(): Char {
    val pieceChar = when (type) {
        PieceType.Pawn -> 'p'
        PieceType.Rook -> 'r'
        PieceType.Knight -> 'n'
        PieceType.Bishop -> 'b'
        PieceType.Queen -> 'q'
        PieceType.King -> 'k'
    }
    return if (color == PieceColor.White) pieceChar.uppercaseChar() else pieceChar
}

/** The FEN castling field, such as "KQkq", or "-" when no right remains. */
fun CastlingRights.toFenField(): String {
    val field = buildString {
        if (whiteKingSide) append('K')
        if (whiteQueenSide) append('Q')
        if (blackKingSide) append('k')
        if (blackQueenSide) append('q')
    }
    return field.ifEmpty { "-" }
}

/** The algebraic name of this square, such as "e4". Row 0 is rank 8. */
fun Position.toAlgebraic(): String = "${'a' + column}${BOARD_SIZE - row}"
