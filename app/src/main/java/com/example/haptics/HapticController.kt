package com.example.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticController(context: Context) {
  private val vibrator: Vibrator? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      manager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }.getOrNull()

  fun tick() {
    performEffect(
      predefined = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_TICK else -1,
      durationMs = 14L,
      amplitude = 90,
    )
  }

  fun click() {
    performEffect(
      predefined = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_CLICK else -1,
      durationMs = 22L,
      amplitude = 140,
    )
  }

  fun heavyClick() {
    performEffect(
      predefined = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_HEAVY_CLICK else -1,
      durationMs = 34L,
      amplitude = 210,
    )
  }

  fun doubleTick() {
    val vib = vibrator ?: return
    if (!vib.hasVibrator()) return
    runCatching {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val timings = longArrayOf(0L, 18L, 45L, 28L)
        val amplitudes = intArrayOf(0, 140, 0, 220)
        vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
      } else {
        @Suppress("DEPRECATION")
        vib.vibrate(longArrayOf(0L, 18L, 45L, 28L), -1)
      }
    }
  }

  fun startAlarmPulse() {
    val vib = vibrator ?: return
    if (!vib.hasVibrator()) return
    runCatching {
      val timings = longArrayOf(0L, 380L, 180L, 380L, 620L)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val amplitudes = intArrayOf(0, 230, 0, 255, 0)
        vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
      } else {
        @Suppress("DEPRECATION")
        vib.vibrate(timings, 0)
      }
    }
  }

  fun stopAlarmPulse() {
    runCatching {
      vibrator?.cancel()
    }
  }

  private fun performEffect(predefined: Int, durationMs: Long, amplitude: Int) {
    val vib = vibrator ?: return
    if (!vib.hasVibrator()) return
    runCatching {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && predefined >= 0) {
        vib.vibrate(VibrationEffect.createPredefined(predefined))
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vib.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
      } else {
        @Suppress("DEPRECATION")
        vib.vibrate(durationMs)
      }
    }
  }
}
