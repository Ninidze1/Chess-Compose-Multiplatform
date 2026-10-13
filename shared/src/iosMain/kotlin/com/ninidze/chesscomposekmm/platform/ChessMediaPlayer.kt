package com.ninidze.chesscomposekmm.platform

import com.ninidze.chesscomposekmm.domain.movement.ActionType
import org.koin.core.component.KoinComponent
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

/**
 * Plays the game sounds with one preloaded [AVAudioPlayer] per clip.
 *
 * The audio session uses the Ambient category, configured once: sounds respect the
 * silent switch and mix with other apps' audio instead of interrupting it.
 */
actual class ChessMediaPlayer : KoinComponent {

    private val players: Map<ActionType, AVAudioPlayer>

    init {
        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategory(AVAudioSessionCategoryAmbient, error = null)
        audioSession.setActive(true, error = null)

        players = ActionType.values()
            .mapNotNull { actionType -> loadPlayer(actionType)?.let { actionType to it } }
            .toMap()
    }

    actual fun playSound(actionType: ActionType) {
        val player = players[actionType] ?: return
        // Restart the clip if it is still playing from the previous call.
        player.currentTime = 0.0
        player.play()
    }

    private fun loadPlayer(actionType: ActionType): AVAudioPlayer? {
        // The pod copies the iosMain/resources/raw folder into the main bundle as "raw".
        val path = NSBundle.mainBundle.pathForResource(
            actionType.soundFileName(),
            ofType = SOUND_FILE_TYPE,
            inDirectory = SOUND_DIRECTORY
        ) ?: return null
        val player = AVAudioPlayer(contentsOfURL = NSURL.fileURLWithPath(path), error = null)
        player.prepareToPlay()
        return player
    }

    // Must match the file names in iosMain/resources/raw (and androidMain/res/raw).
    private fun ActionType.soundFileName(): String = when (this) {
        ActionType.MOVE -> "move_self"
        ActionType.CAPTURE -> "capture"
        ActionType.FINISH -> "game_end"
        ActionType.MOVE_CHECK -> "move_check"
        ActionType.PROMOTION -> "promote"
        ActionType.START -> "notify"
    }

    private companion object {
        const val SOUND_DIRECTORY = "raw"
        const val SOUND_FILE_TYPE = "mp3"
    }
}
