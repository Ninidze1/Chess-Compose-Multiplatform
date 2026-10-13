package com.ninidze.chesscomposekmm.platform

import android.content.Context
import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.SoundPool
import com.ninidze.chesscomposekmm.domain.movement.ActionType
import com.ninidze.chesscomposekmm.domain.sound.SoundPlayer
import com.ninidze.chesscomposekmm.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.IOException
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
        val assets = context.applicationContext.assets
        sampleIds = ActionType.values()
            .mapNotNull { actionType -> loadSample(assets, actionType)?.let { actionType to it } }
            .toMap()
    }

    /**
     * Loads the clip from the Compose resources, which the Android build packages as assets.
     * MP3 files are stored uncompressed, so they can be opened as a file descriptor.
     */
    @OptIn(ExperimentalResourceApi::class)
    private fun loadSample(assets: AssetManager, actionType: ActionType): Int? {
        val assetPath = Res.getUri(actionType.soundResourcePath()).removePrefix(ASSET_URI_PREFIX)
        return try {
            assets.openFd(assetPath).use { descriptor -> soundPool.load(descriptor, SOUND_PRIORITY) }
        } catch (e: IOException) {
            null
        }
    }

    override fun playSound(actionType: ActionType) {
        val sampleId = sampleIds[actionType] ?: return
        // A sound requested before its clip has loaded is skipped rather than delayed.
        if (sampleId !in loadedSamples) return
        soundPool.play(sampleId, FULL_VOLUME, FULL_VOLUME, SOUND_PRIORITY, NO_LOOP, NORMAL_RATE)
    }

    private companion object {
        const val ASSET_URI_PREFIX = "file:///android_asset/"
        const val MAX_STREAMS = 2
        const val SOUND_PRIORITY = 1
        const val LOAD_SUCCESS = 0
        const val FULL_VOLUME = 1f
        const val NO_LOOP = 0
        const val NORMAL_RATE = 1f
    }
}
