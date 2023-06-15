package com.ninidze.chesscomposekmm.domain.engine.search

interface SearchObserver {

    fun info(info: SearchStatusInfo)

    fun bestMove(bestMove: Int, ponder: Int)

}