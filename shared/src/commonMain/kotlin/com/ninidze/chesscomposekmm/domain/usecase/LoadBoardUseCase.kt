package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.repository.IChessGameRepository
import com.ninidze.chesscomposekmm.domain.result.Resource
import com.ninidze.chesscomposekmm.domain.result.safeCall

/** Loads the saved board. Success holds null when there is no usable saved game. */
class LoadBoardUseCase(private val chessBoardRepository: IChessGameRepository) {
    suspend operator fun invoke(): Resource<ChessBoard?> {
        return safeCall { chessBoardRepository.loadBoard() }
    }
}
