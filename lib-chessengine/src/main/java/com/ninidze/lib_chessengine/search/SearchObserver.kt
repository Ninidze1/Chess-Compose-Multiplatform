package com.ninidze.chesscomposekmm.data.engine.search

interface SearchObserver {

    fun info(info: SearchStatusInfo)

    fun bestMove(bestMove: Int, ponder: Int)

}