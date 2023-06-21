package com.ninidze.chesscomposekmm.domain.repository

import com.ninidze.chesscomposekmm.data.helper.Resource
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import kotlinx.coroutines.flow.Flow

interface IChessGameRepository {
    suspend fun saveBoard(board: ChessBoard): Resource<Unit>
    suspend fun loadBoard(): Resource<Flow<ChessBoard>>
}