package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Real-time PCM audio chime synthesizer mirroring the Web Audio C-major arpeggio
 * (C5, E5, G5, C6) with exponential envelope decay.
 */
class ChimeSynthesizer {
  private val sampleRate = 44100
  private var playbackJob: Job? = null
  private val scope = CoroutineScope(Dispatchers.Default)

  fun playChime(tone: String = "marimba", singleBurst: Boolean = false) {
    stop()
    playbackJob = scope.launch {
      val pcmData = renderArpeggioPcm(useTriangle = tone != "marimba")
      do {
        playBuffer(pcmData)
        if (!singleBurst && isActive) {
          delay(550L)
        }
      } while (!singleBurst && isActive)
    }
  }

  fun stop() {
    playbackJob?.cancel()
    playbackJob = null
  }

  private fun renderArpeggioPcm(useTriangle: Boolean): ShortArray {
    val notesHz = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
    val stepOffsetSec = 0.14
    val noteDurSec = 0.50
    val totalSec = stepOffsetSec * (notesHz.size - 1) + noteDurSec
    val totalSamples = (totalSec * sampleRate).toInt()
    val mix = DoubleArray(totalSamples)

    notesHz.forEachIndexed { index, freq ->
      val startSample = (index * stepOffsetSec * sampleRate).toInt()
      val noteSamples = (noteDurSec * sampleRate).toInt()
      for (i in 0 until noteSamples) {
        val sampleIdx = startSample + i
        if (sampleIdx >= totalSamples) break
        val t = i.toDouble() / sampleRate
        val attack = (t / 0.015).coerceAtMost(1.0)
        val decay = exp(-6.5 * t)
        val wave = if (useTriangle) {
          val phase = (freq * t) % 1.0
          4.0 * kotlin.math.abs(phase - 0.5) - 1.0
        } else {
          sin(2.0 * PI * freq * t)
        }
        mix[sampleIdx] += 0.24 * attack * decay * wave
      }
    }

    return ShortArray(totalSamples) { i ->
      val clamped = mix[i].coerceIn(-0.98, 0.98)
      (clamped * Short.MAX_VALUE).toInt().toShort()
    }
  }

  private fun playBuffer(pcm: ShortArray) {
    val minBuf = AudioTrack.getMinBufferSize(
      sampleRate,
      AudioFormat.CHANNEL_OUT_MONO,
      AudioFormat.ENCODING_PCM_16BIT,
    )
    val bufferSize = maxOf(minBuf, pcm.size * 2)
    val track = try {
      AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build(),
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build(),
        )
        .setBufferSizeInBytes(bufferSize)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()
    } catch (_: Throwable) {
      return
    }

    try {
      track.write(pcm, 0, pcm.size)
      track.play()
      val durationMs = (pcm.size * 1000L) / sampleRate
      Thread.sleep(durationMs + 40L)
    } catch (_: InterruptedException) {
      // Stopped cleanly
    } catch (_: Throwable) {
      // AudioTrack unavailable in headless test environments
    } finally {
      runCatching {
        track.stop()
        track.release()
      }
    }
  }
}
