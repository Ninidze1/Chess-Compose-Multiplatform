package com.ninidze.chesscomposekmm.domain.engine.book

/**
 * Opening book support

 * @author rui
 */
interface Book {
    /**
     * Gets a random move from the book taking care of weights
     */
    fun getMove(board: com.ninidze.chesscomposekmm.domain.engine.Board): Int
}
