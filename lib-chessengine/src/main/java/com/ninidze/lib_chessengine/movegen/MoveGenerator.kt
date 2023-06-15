package com.ninidze.chesscomposekmm.data.engine.movegen

import com.ninidze.chesscomposekmm.domain.engine.Board

interface MoveGenerator {

    fun generateMoves(board: Board, moves: IntArray, startIndex: Int): Int

}