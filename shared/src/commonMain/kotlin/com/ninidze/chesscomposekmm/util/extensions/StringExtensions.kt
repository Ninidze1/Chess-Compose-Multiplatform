package com.ninidze.chesscomposekmm.util.extensions

import com.ninidze.chesscomposekmm.domain.model.Position

fun String.toPosition(): Pair<Position, Position> {
    var processedString = this
    if (this.length != 4) {
        processedString = this.dropLast(1)
    }

    val fromColumn = processedString[0].lowercaseChar() - 'a'
    val fromRow = 7 - (processedString[1] - '1')
    val fromPosition = Position(fromRow, fromColumn)

    val toColumn = processedString[2].lowercaseChar() - 'a'
    val toRow = 7 - (processedString[3] - '1')
    val toPosition = Position(toRow, toColumn)

    return Pair(fromPosition, toPosition)
}
