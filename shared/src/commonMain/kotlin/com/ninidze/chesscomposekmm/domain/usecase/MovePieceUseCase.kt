package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.data.helper.Resource
import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.util.Constants.INVALID_MOVE_MESSAGE
import com.ninidze.chesscomposekmm.util.extensions.movePiece

class MovePieceUseCase {
    operator fun invoke(
        chessBoard: ChessBoard,
        piece: ChessPiece,
        targetPosition: Position
    ): Resource<ChessBoard> {
        return if (piece.isValidMove(chessBoard, targetPosition)) {
            val updatedChessBoard = chessBoard.movePiece(piece, targetPosition)
            Resource.Success(updatedChessBoard)
        } else {
            Resource.Failure(INVALID_MOVE_MESSAGE)
        }
    }
}