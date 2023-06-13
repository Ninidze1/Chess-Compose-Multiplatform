package com.ninidze.chesscomposekmm.domain.model

import com.ninidze.chesscomposekmm.util.Constants.BOARD_SIZE

data class Position(val row: Int, val column: Int) {
    fun isValidPosition(): Boolean {
        return row in 0 until BOARD_SIZE && column in 0 until BOARD_SIZE
    }

    operator fun plus(other: Position): Position =
        Position(this.row + other.row, this.column + other.column)
}