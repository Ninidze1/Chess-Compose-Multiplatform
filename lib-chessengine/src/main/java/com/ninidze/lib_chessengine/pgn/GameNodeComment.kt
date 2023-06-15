package com.ninidze.chesscomposekmm.data.engine.pgn

class GameNodeComment(var comment: String) : GameNode() {

    override fun toString(): String {
        return "GameNodeComment{" +
                "comment='" + comment + '\'' +
                "}\n"
    }
}
