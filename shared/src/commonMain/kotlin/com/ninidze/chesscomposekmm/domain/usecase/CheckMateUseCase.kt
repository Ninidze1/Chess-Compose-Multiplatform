package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.util.extensions.isKingChecked
import com.ninidze.chesscomposekmm.util.extensions.movePiece

class CheckMateUseCase {

    operator fun invoke(chessBoard: ChessBoard, color: PieceColor): Boolean {
        if (!chessBoard.isKingChecked(color)) return false

        val possibleMoves = chessBoard.cells.flatten()
            .filter { piece -> piece != null && piece.color == color }
            .flatMap { piece -> piece?.getAvailableMoves(chessBoard) ?: emptyList() }

        return possibleMoves.none { move ->
            val piece = chessBoard.getPieceAtPosition(move) ?: return@none false
            val temporaryBoard = chessBoard.copy().movePiece(piece, move)
            !temporaryBoard.isKingChecked(color)
        }
    }
}
