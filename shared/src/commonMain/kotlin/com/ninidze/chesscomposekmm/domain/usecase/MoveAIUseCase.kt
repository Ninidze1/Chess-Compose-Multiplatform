package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.domain.engine.Move
import com.ninidze.chesscomposekmm.domain.engine.search.SearchEngine
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.movement.ActionType
import com.ninidze.chesscomposekmm.platform.ChessMediaPlayer
import com.ninidze.chesscomposekmm.util.Constants.INVALID_MOVE_MESSAGE
import com.ninidze.chesscomposekmm.util.Constants.PIECE_NOT_FOUND
import com.ninidze.chesscomposekmm.util.FenConverter
import com.ninidze.chesscomposekmm.util.extensions.movePiece
import com.ninidze.chesscomposekmm.util.extensions.toPosition

class MoveAIUseCase(
    private val mediaPlayer: ChessMediaPlayer,
    private val chessBoardConverter: FenConverter,
    private val engine: SearchEngine
) {
    operator fun invoke(chessBoard: ChessBoard): Result<ChessBoard> {
        val calculatedMove = calculateBotMove(chessBoard).toPosition()
        val piece = chessBoard.getPieceAtPosition(calculatedMove.first)
            ?: return Result.failure(IllegalStateException(PIECE_NOT_FOUND))
        val targetPosition = calculatedMove.second

        return if (piece.isValidMove(chessBoard, targetPosition)) {
            mediaPlayer.playSound(ActionType.MOVE)
            val updatedChessBoard = chessBoard.movePiece(piece, targetPosition)
            Result.success(updatedChessBoard)
        } else {
            Result.failure(IllegalStateException(INVALID_MOVE_MESSAGE))
        }
    }

    private fun calculateBotMove(chessBoard: ChessBoard): String {
        engine.board.setupWithFen(
            engine = engine,
            fen = chessBoardConverter.convertToFen(chessBoard)
        )
        return Move.toString(engine.bestMove)
    }

}
