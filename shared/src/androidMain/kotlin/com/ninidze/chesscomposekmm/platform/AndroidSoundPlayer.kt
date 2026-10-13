package com.ninidze.chesscomposekmm.platform

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.ninidze.chesscomposekmm.R
import com.ninidze.chesscomposekmm.domain.movement.ActionType
import com.ninidze.chesscomposekmm.domain.sound.SoundPlayer
import java.util.concurrent.ConcurrentHashMap

/**
 * Plays the game sounds through one [SoundPool]. Every clip is loaded once, when the player
 * is created, so playing a sound allocates nothing and never cuts off the previous one.
 *
 * @param context Used only to load the clips, through its application context; it is not kept.
 */
class AndroidSoundPlayer(context: Context) : SoundPlayer {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Sample ids that finished loading; SoundPool loads asynchronously and reports it on
    // its own thread, so the set must be safe to read from the caller of playSound.
    private val loadedSamples: MutableSet<Int> = ConcurrentHashMap.newKeySet<Int>()

    private val sampleIds: Map<ActionType, Int>

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == LOAD_SUCCESS) loadedSamples += sampleId
        }
        val appContext = context.applicationContext
        sampleIds = ActionType.values().associateWith { actionType ->
            soundPool.load(appContext, actionType.rawResource(), SOUND_PRIORITY)
        }
    }

    override fun playSound(actionType: ActionType) {
        val sampleId = sampleIds[actionType] ?: return
        // A sound requested before its clip has loaded is skipped rather than delayed.
        if (sampleId !in loadedSamples) return
        soundPool.play(sampleId, FULL_VOLUME, FULL_VOLUME, SOUND_PRIORITY, NO_LOOP, NORMAL_RATE)
    }

    private fun ActionType.rawResource(): Int = when (this) {
        ActionType.MOVE -> R.raw.move_self
        ActionType.CAPTURE -> R.raw.capture
        ActionType.FINISH -> R.raw.game_end
        ActionType.MOVE_CHECK -> R.raw.move_check
        ActionType.PROMOTION -> R.raw.promote
        ActionType.START -> R.raw.notify
    }

    private companion object {
        const val MAX_STREAMS = 2
        const val SOUND_PRIORITY = 1
        const val LOAD_SUCCESS = 0
        const val FULL_VOLUME = 1f
        const val NO_LOOP = 0
        const val NORMAL_RATE = 1f
    }
}
