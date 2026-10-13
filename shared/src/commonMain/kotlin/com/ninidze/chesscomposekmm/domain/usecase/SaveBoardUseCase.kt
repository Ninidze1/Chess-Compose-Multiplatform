package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.repository.IChessGameRepository
import com.ninidze.chesscomposekmm.domain.result.Resource
import com.ninidze.chesscomposekmm.domain.result.safeCall

class SaveBoardUseCase(private val chessBoardRepository: IChessGameRepository) {
    suspend operator fun invoke(chessBoard: ChessBoard): Resource<Unit> =
        safeCall { chessBoardRepository.saveBoard(chessBoard) }
}
