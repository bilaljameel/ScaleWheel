package com.scalewheel

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Plays the short "tik" of a [ScaleWheel]. Uses a [SoundPool] with the system's UI-sound
 * attributes, so it follows the device volume and stays silent in silent mode.
 */
internal class TickSound(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private var soundId = 0
    private var loaded = false
    private var lastPlayed = 0L

    init {
        pool.setOnLoadCompleteListener { _, id, status ->
            if (id == soundId && status == 0) loaded = true
        }
        soundId = pool.load(context, R.raw.scalewheel_tick, 1)
    }

    /** Plays one tick at [volume] (0..1). Ticks closer than [MinGapMs] apart are skipped. */
    fun play(volume: Float) {
        if (!loaded) return
        val now = SystemClock.uptimeMillis()
        if (now - lastPlayed < MinGapMs) return
        lastPlayed = now
        val v = volume.coerceIn(0f, 1f)
        pool.play(soundId, v, v, 1, 0, 1f)
    }

    fun release() = pool.release()

    private companion object {
        const val MinGapMs = 25L
    }
}

@Composable
internal fun rememberTickSound(enabled: Boolean): TickSound? {
    val context = LocalContext.current.applicationContext
    val sound = remember(enabled) { if (enabled) TickSound(context) else null }
    DisposableEffect(sound) { onDispose { sound?.release() } }
    return sound
}
