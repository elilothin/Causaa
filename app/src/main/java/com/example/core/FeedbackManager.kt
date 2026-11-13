package com.example.core

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SoundCue {
    NEXT_EXERCISE,
    WORKOUT_DONE,
    RANK_UP,
    WATER_ADD,
    REST_VALIDATED,
}

class FeedbackManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun isSystemReducedMotion(): Boolean {
        return runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            ) == 0f
        }.getOrDefault(false)
    }

    fun vibrateShort(enabled: Boolean) {
        if (!enabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(28L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(28L)
            }
        }
    }

    fun vibrateVictory(enabled: Boolean) {
        if (!enabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0L, 45L, 65L, 95L)
                val amps = intArrayOf(0, 170, 0, 255)
                v.vibrate(VibrationEffect.createWaveform(pattern, amps, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0L, 45L, 65L, 95L), -1)
            }
        }
    }

    fun playCue(cue: SoundCue, enabled: Boolean) {
        if (!enabled) return
        scope.launch {
            runCatching {
                when (cue) {
                    SoundCue.NEXT_EXERCISE -> playNotes(listOf(587.33 to 75, 783.99 to 90))
                    SoundCue.WATER_ADD -> playNotes(listOf(659.25 to 60, 880.00 to 80))
                    SoundCue.REST_VALIDATED -> playNotes(listOf(440.0 to 90, 554.37 to 130))
                    SoundCue.WORKOUT_DONE -> playNotes(listOf(523.25 to 85, 659.25 to 85, 783.99 to 180))
                    SoundCue.RANK_UP -> playNotes(listOf(523.25 to 80, 659.25 to 80, 783.99 to 80, 1046.50 to 220))
                }
            }
        }
    }

    private fun playNotes(notes: List<Pair<Double, Int>>) {
        val sampleRate = 22050
        val totalSamples = notes.sumOf { (sampleRate * it.second) / 1000 }
        if (totalSamples <= 0) return

        val pcm = ShortArray(totalSamples)
        var offset = 0
        for ((freq, durationMs) in notes) {
            val count = (sampleRate * durationMs) / 1000
            for (i in 0 until count) {
                val t = i.toDouble() / sampleRate
                val env = exp(-5.5 * (i.toDouble() / count)) *
                    minOf(1.0, i / 120.0)
                val sample = sin(2.0 * PI * freq * t) * env * 0.35
                pcm[offset + i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            offset += count
        }

        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(minBuf, pcm.size * 2))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(pcm, 0, pcm.size)
        track.play()
        Thread.sleep((notes.sumOf { it.second } + 80).toLong())
        track.stop()
        track.release()
    }
}
