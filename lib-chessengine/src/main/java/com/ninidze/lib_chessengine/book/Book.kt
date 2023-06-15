package com.ninidze.chesscomposekmm.data.engine.book

import com.ninidze.chesscomposekmm.domain.engine.Board

/**
 * Opening book support

 * @author rui
 */
interface Book {
    /**
     * Gets a random move from the book taking care of weights
     */
    fun getMove(board: Board): Int
}
