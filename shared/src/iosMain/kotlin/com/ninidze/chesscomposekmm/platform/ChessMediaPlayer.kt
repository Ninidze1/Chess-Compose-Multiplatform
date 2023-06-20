package com.ninidze.chesscomposekmm.platform

import com.ninidze.chesscomposekmm.domain.movement.ActionType
import org.koin.core.component.KoinComponent
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

actual class ChessMediaPlayer : KoinComponent {
    private var audioPlayer: AVPlayer? = null

    actual fun playSound(actionType: ActionType) {
        val soundFileName = when (actionType) {
            ActionType.MOVE -> "move_self"
            ActionType.CAPTURE -> "capture"
            ActionType.FINISH -> "game_end"
            ActionType.MOVE_CHECK -> "move_check"
            ActionType.PROMOTION -> "promote"
            ActionType.START -> "notify"
        }

        val soundFilePath = NSBundle.mainBundle.pathForResource("raw/$soundFileName", ofType = "mp3")
        soundFilePath?.let {
            val soundFileUrl = NSURL.fileURLWithPath(it)

            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, error = null)
            audioSession.setActive(true, error = null)

            audioPlayer?.pause()
            audioPlayer = AVPlayer.playerWithURL(soundFileUrl)
            audioPlayer?.play()
        }
    }
}


