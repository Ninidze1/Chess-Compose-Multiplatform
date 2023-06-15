package com.ninidze.chesscomposekmm.domain.engine.movegen

interface MoveGenerator {

    fun generateMoves(board: com.ninidze.chesscomposekmm.domain.engine.Board, moves: IntArray, startIndex: Int): Int

}