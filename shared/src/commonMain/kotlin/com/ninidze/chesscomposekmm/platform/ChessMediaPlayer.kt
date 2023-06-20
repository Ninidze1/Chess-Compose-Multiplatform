package com.ninidze.chesscomposekmm.platform

import com.ninidze.chesscomposekmm.domain.movement.ActionType
import org.koin.core.component.KoinComponent

expect class ChessMediaPlayer() : KoinComponent {
    fun playSound(actionType: ActionType)
}