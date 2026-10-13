package com.ninidze.chesscomposekmm.testutil

import com.ninidze.chesscomposekmm.domain.ai.ChessAi
import com.ninidze.chesscomposekmm.domain.model.AiMove
import com.ninidze.chesscomposekmm.domain.model.ChessBoard
import com.ninidze.chesscomposekmm.domain.movement.ActionType
import com.ninidze.chesscomposekmm.domain.repository.IChessGameRepository
import com.ninidze.chesscomposekmm.domain.sound.SoundPlayer

/** A [ChessAi] that answers with [answer] and records every board it was asked about. */
class FakeChessAi(
    private val answer: suspend (ChessBoard) -> AiMove? = { null }
) : ChessAi {
    val requests = mutableListOf<ChessBoard>()

    override suspend fun findBestMove(board: ChessBoard): AiMove? {
        requests += board
        return answer(board)
    }
}

/** A [SoundPlayer] that only records what it was asked to play. */
class FakeSoundPlayer : SoundPlayer {
    val played = mutableListOf<ActionType>()

    override fun playSound(actionType: ActionType) {
        played += actionType
    }
}

/** An in-memory [IChessGameRepository]. */
class FakeChessGameRepository(var savedBoard: ChessBoard? = null) : IChessGameRepository {
    val saves = mutableListOf<ChessBoard>()

    override suspend fun saveBoard(board: ChessBoard) {
        saves += board
        savedBoard = board
    }

    override suspend fun loadBoard(): ChessBoard? = savedBoard
}
