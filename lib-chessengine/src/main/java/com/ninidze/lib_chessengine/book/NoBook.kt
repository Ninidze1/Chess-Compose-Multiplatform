package com.ninidze.chesscomposekmm.data.engine.book

import com.ninidze.chesscomposekmm.domain.engine.Board
import com.ninidze.chesscomposekmm.domain.engine.Move

class NoBook : Book {

    override fun getMove(board: Board): Int {
        return Move.NONE
    }
}