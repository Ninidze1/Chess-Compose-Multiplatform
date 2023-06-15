package com.ninidze.chesscomposekmm.util

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.pieces.Bishop
import com.ninidze.chesscomposekmm.domain.pieces.King
import com.ninidze.chesscomposekmm.domain.pieces.Knight
import com.ninidze.chesscomposekmm.domain.pieces.Pawn
import com.ninidze.chesscomposekmm.domain.pieces.Queen
import com.ninidze.chesscomposekmm.domain.pieces.Rook

class FenConverter {
    fun convertToFen(chessBoard: ChessBoard): String {
        val fenBuilder = StringBuilder()

        appendBoardLayout(chessBoard, fenBuilder)
        appendPlayerTurn(chessBoard, fenBuilder)

        fenBuilder.append(" - - 0 1")
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
                fenBuilder.append(getPieceChar(piece))
            }
        }
        appendEmptySquares(fenBuilder, emptySquares)
    }

    private fun appendEmptySquares(fenBuilder: StringBuilder, emptySquares: Int) {
        if (emptySquares != 0) {
            fenBuilder.append(emptySquares)
        }
    }

    private fun getPieceChar(piece: ChessPiece): Char {
        val pieceChar = when (piece) {
            is Pawn -> 'p'
            is Rook -> 'r'
            is Knight -> 'n'
            is Bishop -> 'b'
            is Queen -> 'q'
            is King -> 'k'
            else -> error("Unknown chess piece type")
        }
        return if (piece.color == PieceColor.White) pieceChar.uppercaseChar() else pieceChar
    }

    private fun appendPlayerTurn(chessBoard: ChessBoard, fenBuilder: StringBuilder) {
        fenBuilder.append(' ')
        fenBuilder.append(if (chessBoard.playerTurn == PieceColor.White) 'w' else 'b')
    }
}
