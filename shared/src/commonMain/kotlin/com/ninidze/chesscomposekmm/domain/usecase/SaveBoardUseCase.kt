package com.ninidze.chesscomposekmm.domain.usecase

import com.ninidze.chesscomposekmm.data.helper.Resource
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.repository.IChessGameRepository

class SaveBoardUseCase(private val chessBoardRepository: IChessGameRepository) {
    suspend operator fun invoke(chessBoard: ChessBoard): Resource<Unit> {
        return chessBoardRepository.saveBoard(chessBoard)
    }
}