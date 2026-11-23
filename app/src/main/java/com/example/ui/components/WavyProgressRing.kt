package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin

/**
 * Material 3 Expressive Wavy Circular Progress Ring.
 * Smoothly modulates wave amplitude between paused (flat) and running (organic sine wave).
 */
@Composable
fun WavyProgressRing(
  progress: Float,
  wavePhase: Float,
  isRunning: Boolean,
  modifier: Modifier = Modifier,
  size: Dp = 264.dp,
  centerContent: @Composable () -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  val clampedProgress = progress.coerceIn(0f, 1f)
  val animatedAmplitude by animateFloatAsState(
    targetValue = if (isRunning) 4.5f else 0f,
    animationSpec = tween(durationMillis = 320),
    label = "wave_amplitude",
  )

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val cx = this.size.width / 2f
      val cy = this.size.height / 2f
      val scale = this.size.minDimension / 260f
      val baseRadius = 110f * scale

      // Track
      drawCircle(
        color = colors.surfaceContainerHighest,
        radius = baseRadius,
        style = Stroke(width = 10f * scale),
      )

      if (clampedProgress > 0.002f) {
        val effectiveProgress = clampedProgress.coerceAtMost(0.9999f)
        val startAngle = -PI / 2.0
        val sweepAngle = PI * 2.0 * effectiveProgress
        val endAngle = startAngle + sweepAngle
        val steps = max(16, floor(220.0 * effectiveProgress).toInt())
        val amp = animatedAmplitude * scale
        val freq = 18.0

        val path = Path()
        for (i in 0..steps) {
          val frac = i.toDouble() / steps
          val angle = startAngle + (endAngle - startAngle) * frac
          val edgeFade = sin(frac * PI)
          val r = baseRadius + amp * sin(freq * angle + wavePhase).toFloat() * edgeFade.toFloat()
          val x = cx + r * cos(angle).toFloat()
          val y = cy + r * sin(angle).toFloat()
          if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
          path = path,
          color = colors.primary,
          style = Stroke(
            width = 8.5f * scale,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
          ),
        )
      }
    }

    centerContent()
  }
}
