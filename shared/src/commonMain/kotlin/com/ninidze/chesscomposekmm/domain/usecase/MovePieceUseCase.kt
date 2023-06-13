package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.util.extensions.movePiece

class MovePieceUseCase {
    operator fun invoke(
        chessBoard: ChessBoard,
        piece: ChessPiece,
        targetPosition: Position
    ): Result<ChessBoard> {
        return if (piece.isValidMove(chessBoard, targetPosition)) {
            val updatedChessBoard = chessBoard.movePiece(piece, targetPosition)
            Result.success(updatedChessBoard)
        } else {
            Result.failure(IllegalStateException("Invalid move"))
        }
    }
}