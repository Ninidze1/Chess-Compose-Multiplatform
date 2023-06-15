package com.ninidze.chesscomposekmm.domain.engine.book

import com.ninidze.chesscomposekmm.domain.engine.Move

class NoBook : Book {

    override fun getMove(board: com.ninidze.chesscomposekmm.domain.engine.Board): Int {
        return Move.NONE
    }
}