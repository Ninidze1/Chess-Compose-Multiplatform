package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.domain.model.ChessBoard

/** Creates the board for a new game. Pure: the caller plays the start sound. */
class StartGameUseCase {
    operator fun invoke(): ChessBoard = ChessBoard.createInitialChessBoard()
}
