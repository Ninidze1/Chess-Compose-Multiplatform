package com.ninidze.chesscomposekmm.data.engine.movegen

import com.ninidze.chesscomposekmm.domain.engine.Board
import com.ninidze.chesscomposekmm.domain.engine.Move

class LegalMoveGenerator : MagicMoveGenerator() {

    /**
     * Get only LEGAL moves testing with doMove
     * The moves are returned with the check flag set
     */
    override fun generateMoves(board: Board, moves: IntArray, startIndex: Int): Int {
        val lastIndex = super.generateMoves(board, moves, startIndex)
        var j = startIndex
        for (i in 0..lastIndex - 1) {
            if (board.doMove(moves[i], true, false)) {
                moves[j++] = if (board.check) moves[i] or Move.CHECK_MASK else moves[i]
                board.undoMove()
            }
        }
        return j
    }
}