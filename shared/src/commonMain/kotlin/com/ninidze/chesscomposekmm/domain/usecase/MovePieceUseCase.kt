package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.PieceColor
import com.ninidze.chesscomposekmm.domain.model.PieceType
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.result.Resource
import com.ninidze.chesscomposekmm.util.Constants.GAME_ALREADY_FINISHED
import com.ninidze.chesscomposekmm.util.Constants.INVALID_MOVE_MESSAGE
import com.ninidze.chesscomposekmm.util.Constants.NOT_PLAYERS_TURN
import com.ninidze.chesscomposekmm.util.Constants.PIECE_NOT_FOUND
import com.ninidze.chesscomposekmm.util.extensions.findPiece
import com.ninidze.chesscomposekmm.util.extensions.isEnPassantCapture
import com.ninidze.chesscomposekmm.util.extensions.movePiece
import com.ninidze.chesscomposekmm.util.extensions.opposite
import com.ninidze.chesscomposekmm.util.extensions.positionKey
import kotlin.math.abs

/**
 * Applies a move for the side to move. On success the returned board has the piece moved
 * (with the rook for castling and the captured pawn removed for en passant), the turn
 * switched to the opponent, the castling rights, en-passant target, move counters and
 * position history updated, and [ChessBoard.result] evaluated for the new side to move.
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

        val movedBoard = chessBoard.nextBoard(boardPiece, targetPosition, promotion)
        return Resource.Success(movedBoard.copy(result = checkGameEndUseCase(movedBoard)))
    }

    /** The board after a validated move, with all game state advanced except the result. */
    private fun ChessBoard.nextBoard(
        piece: ChessPiece,
        targetPosition: Position,
        promotion: PieceType
    ): ChessBoard {
        val from = piece.position
        val isPawnMove = piece.type == PieceType.Pawn
        val isCapture = getPieceAtPosition(targetPosition) != null ||
            isEnPassantCapture(piece, targetPosition)
        val isIrreversible = isPawnMove || isCapture

        val newEnPassantTarget = if (isPawnMove && abs(targetPosition.row - from.row) == 2) {
            Position((from.row + targetPosition.row) / 2, from.column)
        } else {
            null
        }

        val moved = movePiece(piece, targetPosition, promotion).copy(
            playerTurn = playerTurn.opposite(),
            castlingRights = castlingRights
                .withoutRightsAt(from)
                .withoutRightsAt(targetPosition),
            enPassantTarget = newEnPassantTarget,
            halfmoveClock = if (isIrreversible) 0 else halfmoveClock + 1,
            fullmoveNumber = if (playerTurn == PieceColor.Black) fullmoveNumber + 1 else fullmoveNumber
        )
        // Positions before a capture or pawn move can never recur, so the history restarts.
        val history = if (isIrreversible) emptyList<String>() else positionHistory
        return moved.copy(positionHistory = history + moved.positionKey())
    }
}
