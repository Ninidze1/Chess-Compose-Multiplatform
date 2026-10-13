package com.ninidze.chesscomposekmm.platform

import com.ninidze.chesscomposekmm.domain.movement.ActionType
import com.ninidze.chesscomposekmm.domain.sound.SoundPlayer
import com.ninidze.chesscomposekmm.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.setActive
import platform.Foundation.NSURL

/**
 * Plays the game sounds with one preloaded [AVAudioPlayer] per clip.
 *
 * The audio session uses the Ambient category, configured once: sounds respect the
 * silent switch and mix with other apps' audio instead of interrupting it.
 */
class IosSoundPlayer : SoundPlayer {

    private val players: Map<ActionType, AVAudioPlayer>

    init {
        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategory(AVAudioSessionCategoryAmbient, error = null)
        audioSession.setActive(true, error = null)

        players = ActionType.values()
            .mapNotNull { actionType -> loadPlayer(actionType)?.let { actionType to it } }
            .toMap()
    }

    override fun playSound(actionType: ActionType) {
        val player = players[actionType] ?: return
        // Restart the clip if it is still playing from the previous call.
        player.currentTime = 0.0
        player.play()
    }

    @OptIn(ExperimentalResourceApi::class)
    private fun loadPlayer(actionType: ActionType): AVAudioPlayer? {
        // The clips are Compose resources (commonMain/composeResources/files/sounds), which
        // the Compose plugin copies into the app bundle through the pod.
        val uri = Res.getUri("$SOUND_DIRECTORY/${actionType.soundFileName()}.$SOUND_FILE_TYPE")
        val url = if (uri.startsWith(FILE_SCHEME)) NSURL.URLWithString(uri) else NSURL.fileURLWithPath(uri)
        if (url == null) return null
        val player = AVAudioPlayer(contentsOfURL = url, error = null)
        player.prepareToPlay()
        return player
    }

    // Must match the file names in commonMain/composeResources/files/sounds (and
    // androidMain/res/raw).
    private fun ActionType.soundFileName(): String = when (this) {
        ActionType.MOVE -> "move_self"
        ActionType.CAPTURE -> "capture"
        ActionType.FINISH -> "game_end"
        ActionType.MOVE_CHECK -> "move_check"
        ActionType.PROMOTION -> "promote"
        ActionType.START -> "notify"
    }

    private companion object {
        const val SOUND_DIRECTORY = "files/sounds"
        const val SOUND_FILE_TYPE = "mp3"
        const val FILE_SCHEME = "file:"
    }
}
