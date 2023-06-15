package com.ninidze.chesscomposekmm.domain.engine.pgn

class GameNodeVariation : GameNode() {

    var variation = ArrayList<GameNode>()

    operator fun get(index: Int): GameNode {
        return variation[index]
    }

    override fun toString(): String {
        return "GameNodeVariation{" +
                "variation=" + variation +
                "}\n"
    }
}
