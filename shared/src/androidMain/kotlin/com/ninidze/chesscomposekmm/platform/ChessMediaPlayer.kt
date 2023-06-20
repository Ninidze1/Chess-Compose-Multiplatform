package com.ninidze.chesscomposekmm.platform

import android.content.Context
import android.media.MediaPlayer
import com.ninidze.chesscomposekmm.R
import com.ninidze.chesscomposekmm.domain.movement.ActionType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class ChessMediaPlayer : KoinComponent {
    private val context: Context by inject()
    private var player: MediaPlayer? = null

    actual fun playSound(actionType: ActionType) {
        player?.release()

        player = when (actionType) {
            ActionType.MOVE -> MediaPlayer.create(context, R.raw.move_self)
            ActionType.CAPTURE -> MediaPlayer.create(context, R.raw.capture)
            ActionType.FINISH -> MediaPlayer.create(context, R.raw.game_end)
            ActionType.MOVE_CHECK -> MediaPlayer.create(context, R.raw.move_heck)
            ActionType.PROMOTION -> MediaPlayer.create(context, R.raw.promote)
            ActionType.START -> MediaPlayer.create(context, R.raw.notify)
        }
        player?.start()
    }
}
