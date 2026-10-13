package com.ninidze.chesscomposekmm.util

import com.ninidze.chesscomposekmm.domain.model.CastlingRights
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.util.extensions.fenChar
import com.ninidze.chesscomposekmm.util.extensions.toAlgebraic
import com.ninidze.chesscomposekmm.util.extensions.toFenField

class FenConverter {
    fun convertToFen(chessBoard: ChessBoard): String {
        val fenBuilder = StringBuilder()

        appendBoardLayout(chessBoard, fenBuilder)
        appendPlayerTurn(chessBoard, fenBuilder)
        appendStateFields(chessBoard, fenBuilder)

        return fenBuilder.toString()
    }

    private fun appendBoardLayout(chessBoard: ChessBoard, fenBuilder: StringBuilder) {
        for (row in 0 until Constants.BOARD_SIZE) {
            appendRowLayout(chessBoard, fenBuilder, row)
            if (row != Constants.BOARD_SIZE - 1) {
                fenBuilder.append('/')
            }
        }
    }

    private fun appendRowLayout(chessBoard: ChessBoard, fenBuilder: StringBuilder, row: Int) {
        var emptySquares = 0
        for (column in 0 until Constants.BOARD_SIZE) {
            val piece = chessBoard.getPieceAtPosition(Position(row, column))
            if (piece == null) {
                emptySquares++
            } else {
                appendEmptySquares(fenBuilder, emptySquares)
                emptySquares = 0
                fenBuilder.append(piece.fenChar())
            }
        }
        appendEmptySquares(fenBuilder, emptySquares)
    }

    private fun appendEmptySquares(fenBuilder: StringBuilder, emptySquares: Int) {
        if (emptySquares != 0) {
            fenBuilder.append(emptySquares)
        }
    }

    private fun appendPlayerTurn(chessBoard: ChessBoard, fenBuilder: StringBuilder) {
        fenBuilder.append(' ')
        fenBuilder.append(if (chessBoard.playerTurn == PieceColor.White) 'w' else 'b')
    }

    /** Castling rights, en-passant square, halfmove clock and fullmove number. */
    private fun appendStateFields(chessBoard: ChessBoard, fenBuilder: StringBuilder) {
        fenBuilder.append(' ')
        fenBuilder.append(consistentCastlingRights(chessBoard).toFenField())
        fenBuilder.append(' ')
        fenBuilder.append(chessBoard.enPassantTarget?.toAlgebraic() ?: "-")
        fenBuilder.append(' ')
        fenBuilder.append(chessBoard.halfmoveClock.coerceAtLeast(0))
        fenBuilder.append(' ')
        fenBuilder.append(chessBoard.fullmoveNumber.coerceAtLeast(1))
    }

    /**
     * The board's castling rights, minus any whose king or rook is not on its home square.
     * The engine would otherwise read a right with a displaced king or rook as Chess960
     * castling.
     */
    private fun consistentCastlingRights(chessBoard: ChessBoard): CastlingRights {
        val rights = chessBoard.castlingRights
        fun hasPiece(color: PieceColor, type: PieceType, column: Int): Boolean {
            val piece = chessBoard.getPieceAtPosition(Position(CastlingRights.homeRow(color), column))
            return piece != null && piece.color == color && piece.type == type
        }
        fun keeps(color: PieceColor, kingSide: Boolean): Boolean {
            val rookColumn =
                if (kingSide) CastlingRights.KING_SIDE_ROOK_COLUMN else CastlingRights.QUEEN_SIDE_ROOK_COLUMN
            return rights.canCastle(color, kingSide) &&
                hasPiece(color, PieceType.King, CastlingRights.KING_COLUMN) &&
                hasPiece(color, PieceType.Rook, rookColumn)
        }
        return CastlingRights(
            whiteKingSide = keeps(PieceColor.White, kingSide = true),
            whiteQueenSide = keeps(PieceColor.White, kingSide = false),
            blackKingSide = keeps(PieceColor.Black, kingSide = true),
            blackQueenSide = keeps(PieceColor.Black, kingSide = false)
        )
    }
}
