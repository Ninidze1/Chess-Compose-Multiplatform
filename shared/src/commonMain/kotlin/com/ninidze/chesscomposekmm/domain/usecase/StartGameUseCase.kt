package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.domain.base.ChessPiece
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.model.Position
import com.ninidze.chesscomposekmm.domain.movement.ActionType
import com.ninidze.chesscomposekmm.platform.ChessMediaPlayer
import com.ninidze.chesscomposekmm.util.Constants.INVALID_MOVE_MESSAGE
import com.ninidze.chesscomposekmm.util.extensions.movePiece

class StartGameUseCase(private val mediaPlayer: ChessMediaPlayer) {
    operator fun invoke(): ChessBoard {
        mediaPlayer.playSound(ActionType.START)
        return ChessBoard.createInitialChessBoard()
    }
}