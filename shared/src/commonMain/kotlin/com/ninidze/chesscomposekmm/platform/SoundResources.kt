package com.ninidze.chesscomposekmm.platform

import com.ninidze.chesscomposekmm.domain.movement.ActionType

/**
 * Path of this action's clip in commonMain/composeResources, for Res.getUri. Both platforms
 * play the same files, so they are bundled once.
 */
internal fun ActionType.soundResourcePath(): String {
    val fileName = when (this) {
        ActionType.MOVE -> "move_self"
        ActionType.CAPTURE -> "capture"
        ActionType.FINISH -> "game_end"
        ActionType.MOVE_CHECK -> "move_check"
        ActionType.PROMOTION -> "promote"
        ActionType.START -> "notify"
    }
    return "files/sounds/$fileName.mp3"
}
