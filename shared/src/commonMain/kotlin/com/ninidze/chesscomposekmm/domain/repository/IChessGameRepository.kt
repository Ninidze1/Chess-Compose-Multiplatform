package com.ninidze.chesscomposekmm.domain.repository

import com.ninidze.chesscomposekmm.domain.model.ChessBoard

interface IChessGameRepository {
    /** Persists [board], replacing any previously saved board. Throws if storage fails. */
    suspend fun saveBoard(board: ChessBoard)

    /**
     * Returns the saved board, or null if none is saved or the saved data cannot be
     * decoded (the unreadable data is discarded). Throws if storage itself fails.
     */
    suspend fun loadBoard(): ChessBoard?
}
