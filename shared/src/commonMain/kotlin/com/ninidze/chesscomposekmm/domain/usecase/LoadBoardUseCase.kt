package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.data.helper.Resource
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.repository.IChessGameRepository
import kotlinx.coroutines.flow.Flow

class LoadBoardUseCase(private val chessBoardRepository: IChessGameRepository) {
    suspend operator fun invoke(): Resource<Flow<ChessBoard>> {
        return chessBoardRepository.loadBoard()
    }
}