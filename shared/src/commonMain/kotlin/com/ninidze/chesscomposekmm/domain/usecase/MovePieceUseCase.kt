package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.data.helper.Resource
import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.util.Constants.GAME_ALREADY_FINISHED
import com.ninidze.chesscomposekmm.util.Constants.INVALID_MOVE_MESSAGE
import com.ninidze.chesscomposekmm.util.Constants.NOT_PLAYERS_TURN
import com.ninidze.chesscomposekmm.util.Constants.PIECE_NOT_FOUND
import com.ninidze.chesscomposekmm.util.extensions.findPiece
import com.ninidze.chesscomposekmm.util.extensions.movePiece
import com.ninidze.chesscomposekmm.util.extensions.opposite

/**
 * Applies a move for the side to move. On success the returned board has the piece moved,
 * the turn switched to the opponent and [ChessBoard.result] evaluated for the new side to move.
 */
class MovePieceUseCase(private val checkGameEndUseCase: CheckGameEndUseCase) {
    operator fun invoke(
        chessBoard: ChessBoard,
        piece: ChessPiece,
        targetPosition: Position,
        promotion: PieceType = PieceType.Queen
    ): Resource<ChessBoard> {
        if (chessBoard.isGameOver) {
            return Resource.Failure(GAME_ALREADY_FINISHED)
        }
        val boardPiece = chessBoard.findPiece(piece)
            ?: return Resource.Failure(PIECE_NOT_FOUND)
        if (boardPiece.color != chessBoard.playerTurn) {
            return Resource.Failure(NOT_PLAYERS_TURN)
        }
        if (!boardPiece.isValidMove(chessBoard, targetPosition)) {
            return Resource.Failure(INVALID_MOVE_MESSAGE)
        }

        val movedBoard = chessBoard
            .movePiece(boardPiece, targetPosition, promotion)
            .copy(playerTurn = chessBoard.playerTurn.opposite())
        return Resource.Success(movedBoard.copy(result = checkGameEndUseCase(movedBoard)))
    }
}
