package com.ninidze.chesscomposekmm.domain.sound

import com.ninidze.chesscomposekmm.domain.movement.ActionType

/**
 * Plays the game sounds. Each platform provides its own implementation through Koin, so
 * the domain and presentation layers never depend on a platform audio API.
 */
interface SoundPlayer {
    /** Plays the sound for [actionType]; does nothing if that sound is not available. */
    fun playSound(actionType: ActionType)
}
