package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DialShape
import com.example.model.HandStyle
import com.example.model.SweepMode
import com.example.ui.theme.LocalExpressiveTokens
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

private const val SHAPE_SAMPLES = 180

fun computeMaterialShapePoints(
  shape: DialShape,
  cx: Float,
  cy: Float,
  radius: Float,
): List<Offset> {
  return List(SHAPE_SAMPLES) { i ->
    val t = (i.toDouble() / SHAPE_SAMPLES) * PI * 2.0
    if (shape == DialShape.SQUIRCLE) {
      val c = cos(t)
      val s = sin(t)
      val x = cx + radius * sign(c).toFloat() * abs(c).pow(0.46).toFloat()
      val y = cy + radius * sign(s).toFloat() * abs(s).pow(0.46).toFloat()
      Offset(x, y)
    } else {
      val r = when (shape) {
        DialShape.COOKIE_9 -> radius * (0.93 + 0.07 * cos(9.0 * t)).toFloat()
        DialShape.CLOVER_4 -> radius * (0.79 + 0.21 * (0.5 - 0.5 * cos(4.0 * t)).pow(0.72)).toFloat()
        DialShape.FLOWER -> radius * (0.87 + 0.13 * cos(8.0 * t)).toFloat()
        DialShape.SOFT_BURST -> radius * (0.84 + 0.16 * (0.5 + 0.5 * cos(10.0 * t)).pow(1.4)).toFloat()
        DialShape.SUNNY -> radius * (0.85 + 0.15 * cos(12.0 * t)).toFloat()
        DialShape.CIRCLE -> radius
        DialShape.SQUIRCLE -> radius
      }
      Offset(
        x = cx + r * cos(t).toFloat(),
        y = cy + r * sin(t).toFloat(),
      )
    }
  }
}

fun buildClosedPath(points: List<Offset>): Path {
  val path = Path()
  if (points.isEmpty()) return path
  path.moveTo(points[0].x, points[0].y)
  for (i in 1 until points.size) {
    path.lineTo(points[i].x, points[i].y)
  }
  path.close()
  return path
}

@Composable
fun AnalogClockDial(
  nowMillis: Long,
  dialShape: DialShape,
  handStyle: HandStyle,
  sweepMode: SweepMode,
  showSeconds: Boolean,
  showNumbers: Boolean,
  onTapDial: () -> Unit,
  dialDescription: String,
  modifier: Modifier = Modifier,
  size: Dp = 240.dp,
) {
  val tokens = LocalExpressiveTokens.current
  val colors = MaterialTheme.colorScheme
  val textMeasurer = rememberTextMeasurer()
  val haptic = LocalHapticFeedback.current
  val view = LocalView.current

  // Spring-physics shape morphing between MaterialShapes
  var previousShape by remember { mutableStateOf(dialShape) }
  var currentTargetShape by remember { mutableStateOf(dialShape) }
  val morphProgress = remember { Animatable(1f) }

  LaunchedEffect(dialShape) {
    if (dialShape != currentTargetShape) {
      previousShape = currentTargetShape
      currentTargetShape = dialShape
      morphProgress.snapTo(0f)
      morphProgress.animateTo(
        targetValue = 1f,
        animationSpec = spring(
          dampingRatio = 0.58f,
          stiffness = Spring.StiffnessMediumLow,
        ),
      )
    }
  }

  val calendar = remember(nowMillis) {
    Calendar.getInstance().apply { timeInMillis = nowMillis }
  }
  val hrs = calendar.get(Calendar.HOUR_OF_DAY)
  val mins = calendar.get(Calendar.MINUTE)
  val secs = calendar.get(Calendar.SECOND)
  val ms = calendar.get(Calendar.MILLISECOND)

  val secVal = if (sweepMode == SweepMode.SMOOTH) secs + ms / 1000f else secs.toFloat()
  val minVal = mins + secVal / 60f
  val hrVal = (hrs % 12) + minVal / 60f

  val secDeg = secVal * 6f
  val minDeg = minVal * 6f
  val hrDeg = hrVal * 30f

  val numberStyle = remember(tokens.displayFamily, colors.onSurface) {
    TextStyle(
      fontFamily = tokens.displayFamily,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = colors.onSurface,
    )
  }

  Box(
    modifier = modifier
      .size(size)
      .clip(androidx.compose.foundation.shape.CircleShape)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = false, radius = size / 2),
        role = Role.Button,
        onClick = {
          view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          onTapDial()
        },
      )
      .semantics { contentDescription = dialDescription },
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val cx = this.size.width / 2f
      val cy = this.size.height / 2f
      val scale = this.size.minDimension / 200f
      val dialRadius = 88f * scale

      // Compute morphed polygon path
      val fromPts = computeMaterialShapePoints(previousShape, cx, cy, dialRadius)
      val toPts = computeMaterialShapePoints(currentTargetShape, cx, cy, dialRadius)
      val t = morphProgress.value
      val morphedPts = List(SHAPE_SAMPLES) { idx ->
        val a = fromPts[idx]
        val b = toPts[idx]
        Offset(
          x = a.x + (b.x - a.x) * t,
          y = a.y + (b.y - a.y) * t,
        )
      }
      val dialPath = buildClosedPath(morphedPts)

      drawPath(
        path = dialPath,
        color = colors.surfaceContainerHighest,
      )
      drawPath(
        path = dialPath,
        color = colors.outlineVariant,
        style = Stroke(width = 2.2f * scale),
      )

      // Hour ticks & optional 12/3/6/9 numbers
      for (i in 0 until 12) {
        val angleRad = ((i * 30f) - 90f) * (PI / 180.0)
        val isMajor = i % 3 == 0
        val r1 = (if (isMajor) 67f else 71f) * scale
        val r2 = 76f * scale
        drawLine(
          color = if (isMajor) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.45f),
          start = Offset(
            x = cx + r1 * cos(angleRad).toFloat(),
            y = cy + r1 * sin(angleRad).toFloat(),
          ),
          end = Offset(
            x = cx + r2 * cos(angleRad).toFloat(),
            y = cy + r2 * sin(angleRad).toFloat(),
          ),
          strokeWidth = (if (isMajor) 3.5f else 2f) * scale,
          cap = StrokeCap.Round,
        )

        if (showNumbers && isMajor) {
          val label = if (i == 0) "12" else i.toString()
          val numR = 54f * scale
          val measured = textMeasurer.measure(label, style = numberStyle)
          val tx = cx + numR * cos(angleRad).toFloat() - measured.size.width / 2f
          val ty = cy + numR * sin(angleRad).toFloat() - measured.size.height / 2f
          drawText(textLayoutResult = measured, topLeft = Offset(tx, ty))
        }
      }

      val hourWidth = when (handStyle) {
        HandStyle.CHUNKY -> 9f
        HandStyle.NEEDLE -> 4.5f
        HandStyle.ORBIT -> 7.5f
      } * scale

      val minuteWidth = when (handStyle) {
        HandStyle.CHUNKY -> 6.5f
        HandStyle.NEEDLE -> 3f
        HandStyle.ORBIT -> 5f
      } * scale

      // Hour Hand
      rotate(degrees = hrDeg, pivot = Offset(cx, cy)) {
        drawLine(
          color = colors.onSurface,
          start = Offset(cx, cy),
          end = Offset(cx, cy - 45f * scale),
          strokeWidth = hourWidth,
          cap = StrokeCap.Round,
        )
      }

      // Minute Hand
      rotate(degrees = minDeg, pivot = Offset(cx, cy)) {
        drawLine(
          color = colors.primary,
          start = Offset(cx, cy),
          end = Offset(cx, cy - 66f * scale),
          strokeWidth = minuteWidth,
          cap = StrokeCap.Round,
        )
      }

      // Second Hand / Orbit Dot
      if (showSeconds) {
        rotate(degrees = secDeg, pivot = Offset(cx, cy)) {
          if (handStyle == HandStyle.ORBIT) {
            drawCircle(
              color = colors.tertiary,
              radius = 5.5f * scale,
              center = Offset(cx, cy - 72f * scale),
            )
          } else {
            drawLine(
              color = colors.tertiary,
              start = Offset(cx, cy + 14f * scale),
              end = Offset(cx, cy - 74f * scale),
              strokeWidth = 2.4f * scale,
              cap = StrokeCap.Round,
            )
          }
        }
      }

      // Center Cap
      drawCircle(
        color = colors.surfaceContainerHighest,
        radius = 8.5f * scale,
        center = Offset(cx, cy),
      )
      drawCircle(
        color = colors.tertiary,
        radius = 5.8f * scale,
        center = Offset(cx, cy),
      )
    }
  }
}

@Composable
fun ShapePreviewIcon(
  shape: DialShape,
  tint: Color,
  modifier: Modifier = Modifier,
  size: Dp = 32.dp,
) {
  Canvas(modifier = modifier.size(size)) {
    val cx = this.size.width / 2f
    val cy = this.size.height / 2f
    val radius = this.size.minDimension * 0.42f
    val pts = computeMaterialShapePoints(shape, cx, cy, radius)
    drawPath(path = buildClosedPath(pts), color = tint)
  }
}
