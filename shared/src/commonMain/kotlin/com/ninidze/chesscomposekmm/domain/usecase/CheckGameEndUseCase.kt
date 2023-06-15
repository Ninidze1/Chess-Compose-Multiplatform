package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.util.extensions.isKingCheckedAfterMove

class CheckGameEndUseCase {
    operator fun invoke(chessBoard: ChessBoard, color: PieceColor): Boolean {
        val allPieces = chessBoard.cells.flatten()
            .filterNotNull()
            .filter { it.color != color }
        val totalPossibleMoves = allPieces.flatMap { it.getAvailableMoves(chessBoard) }
        if (totalPossibleMoves.isEmpty()) return true

        for (piece in allPieces) {
            val possibleMovesForPiece = piece.getAvailableMoves(chessBoard)
            for (move in possibleMovesForPiece) {
                if (!chessBoard.isKingCheckedAfterMove(piece, move)) {
                    return false
                }
            }
        }

        return true
    }
}
