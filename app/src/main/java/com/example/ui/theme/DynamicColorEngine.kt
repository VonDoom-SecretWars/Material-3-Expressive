package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.ClockThemeConfig
import com.example.model.CornerStyle
import com.example.model.PaletteScheme
import com.example.model.SurfaceMode
import kotlin.math.max
import kotlin.math.min

data class HslColor(val h: Float, val s: Float, val l: Float)

data class ExpressiveRadii(
  val xs: Dp,
  val sm: Dp,
  val md: Dp,
  val lg: Dp,
  val xl: Dp,
)

object DynamicColorEngine {

  fun parseHexColor(hex: String, fallback: Color = Color(0xFF16C768)): Color {
    return runCatching {
      val clean = hex.removePrefix("#")
      val value = clean.toLong(16)
      when (clean.length) {
        6 -> Color((0xFF000000L or value).toInt())
        8 -> Color(value.toInt())
        else -> fallback
      }
    }.getOrDefault(fallback)
  }

  fun colorToHex(color: Color): String {
    val r = (color.red * 255f).toInt().coerceIn(0, 255)
    val g = (color.green * 255f).toInt().coerceIn(0, 255)
    val b = (color.blue * 255f).toInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", r, g, b)
  }

  fun hexToHsl(hex: String): HslColor {
    val c = parseHexColor(hex)
    val r = c.red
    val g = c.green
    val b = c.blue
    val maxC = max(r, max(g, b))
    val minC = min(r, min(g, b))
    val l = (maxC + minC) / 2f
    var h = 0f
    var s = 0f
    if (maxC != minC) {
      val d = maxC - minC
      s = if (l > 0.5f) d / (2f - maxC - minC) else d / (maxC + minC)
      h = when (maxC) {
        r -> (g - b) / d + (if (g < b) 6f else 0f)
        g -> (b - r) / d + 2f
        else -> (r - g) / d + 4f
      }
      h *= 60f
    }
    return HslColor(h = h, s = s * 100f, l = l * 100f)
  }

  fun hslColor(h: Float, s: Float, l: Float): Color {
    val hue = ((h % 360f) + 360f) % 360f
    val sat = (s.coerceIn(0f, 100f)) / 100f
    val lig = (l.coerceIn(0f, 100f)) / 100f
    return Color.hsl(hue = hue, saturation = sat, lightness = lig)
  }

  fun generateColorScheme(config: ClockThemeConfig): ColorScheme {
    val base = hexToHsl(config.seedColorHex)
    val isDark = config.surface != SurfaceMode.LIGHT
    val isAmoled = config.surface == SurfaceMode.AMOLED

    var pSat = (if (base.s == 0f) 70f else base.s).coerceIn(45f, 92f)
    var secHue = base.h + 18f
    var secSat = pSat * 0.35f
    var terHue = base.h + 65f
    var terSat = pSat * 0.65f

    when (config.scheme) {
      PaletteScheme.EXPRESSIVE -> {
        pSat = min(95f, pSat * 1.08f)
        secHue = base.h + 26f
        secSat = min(60f, pSat * 0.52f)
        terHue = base.h + 125f
        terSat = min(90f, pSat * 0.85f)
      }
      PaletteScheme.VIBRANT -> {
        pSat = 94f
        secHue = base.h + 12f
        secSat = 72f
        terHue = base.h - 55f
        terSat = 90f
      }
      PaletteScheme.TONAL -> Unit
      PaletteScheme.MONO -> {
        pSat = 0f
        secSat = 0f
        terSat = 0f
      }
    }

    val neutralSat = when {
      config.scheme == PaletteScheme.MONO -> 0f
      isAmoled -> 8f
      else -> 14f
    }

    return if (isDark) {
      val surfaceBase = if (isAmoled) Color(0xFF000000) else hslColor(base.h, neutralSat, 7f)
      darkColorScheme(
        primary = hslColor(base.h, pSat, 78f),
        onPrimary = hslColor(base.h, pSat, 18f),
        primaryContainer = hslColor(base.h, pSat * 0.72f, 30f),
        onPrimaryContainer = hslColor(base.h, pSat, 92f),
        secondary = hslColor(secHue, secSat, 78f),
        onSecondary = hslColor(secHue, secSat, 20f),
        secondaryContainer = hslColor(secHue, secSat * 0.8f, 28f),
        onSecondaryContainer = hslColor(secHue, secSat, 92f),
        tertiary = hslColor(terHue, terSat, 78f),
        onTertiary = hslColor(terHue, terSat, 18f),
        tertiaryContainer = hslColor(terHue, terSat * 0.75f, 30f),
        onTertiaryContainer = hslColor(terHue, terSat, 92f),
        error = Color(0xFFF2B8B5),
        onError = Color(0xFF601410),
        background = surfaceBase,
        onBackground = hslColor(base.h, 12f, 92f),
        surface = surfaceBase,
        onSurface = hslColor(base.h, 12f, 92f),
        surfaceVariant = if (isAmoled) Color(0xFF17171D) else hslColor(base.h, neutralSat, 17f),
        onSurfaceVariant = hslColor(base.h, 12f, 74f),
        surfaceContainerLowest = if (isAmoled) Color(0xFF000000) else hslColor(base.h, neutralSat, 5f),
        surfaceContainerLow = if (isAmoled) Color(0xFF08080A) else hslColor(base.h, neutralSat, 10f),
        surfaceContainer = if (isAmoled) Color(0xFF101014) else hslColor(base.h, neutralSat, 13f),
        surfaceContainerHigh = if (isAmoled) Color(0xFF17171D) else hslColor(base.h, neutralSat, 17f),
        surfaceContainerHighest = if (isAmoled) Color(0xFF202028) else hslColor(base.h, neutralSat, 22f),
        outline = hslColor(base.h, 10f, 56f),
        outlineVariant = hslColor(base.h, 12f, 26f),
      )
    } else {
      val surfaceBase = hslColor(base.h, neutralSat, 98f)
      lightColorScheme(
        primary = hslColor(base.h, pSat, 42f),
        onPrimary = Color.White,
        primaryContainer = hslColor(base.h, pSat, 90f),
        onPrimaryContainer = hslColor(base.h, pSat, 16f),
        secondary = hslColor(secHue, secSat, 44f),
        onSecondary = Color.White,
        secondaryContainer = hslColor(secHue, secSat, 90f),
        onSecondaryContainer = hslColor(secHue, secSat, 18f),
        tertiary = hslColor(terHue, terSat, 44f),
        onTertiary = Color.White,
        tertiaryContainer = hslColor(terHue, terSat, 90f),
        onTertiaryContainer = hslColor(terHue, terSat, 18f),
        error = Color(0xFFB3261E),
        onError = Color.White,
        background = surfaceBase,
        onBackground = hslColor(base.h, 16f, 12f),
        surface = surfaceBase,
        onSurface = hslColor(base.h, 16f, 12f),
        surfaceVariant = hslColor(base.h, neutralSat, 89f),
        onSurfaceVariant = hslColor(base.h, 14f, 34f),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = hslColor(base.h, neutralSat, 95f),
        surfaceContainer = hslColor(base.h, neutralSat, 92f),
        surfaceContainerHigh = hslColor(base.h, neutralSat, 89f),
        surfaceContainerHighest = hslColor(base.h, neutralSat, 85f),
        outline = hslColor(base.h, 12f, 48f),
        outlineVariant = hslColor(base.h, 14f, 80f),
      )
    }
  }

  fun radiiFor(style: CornerStyle): ExpressiveRadii = when (style) {
    CornerStyle.EXPRESSIVE -> ExpressiveRadii(xs = 10.dp, sm = 16.dp, md = 24.dp, lg = 32.dp, xl = 44.dp)
    CornerStyle.SOFT -> ExpressiveRadii(xs = 6.dp, sm = 10.dp, md = 16.dp, lg = 22.dp, xl = 28.dp)
    CornerStyle.SHARP -> ExpressiveRadii(xs = 2.dp, sm = 4.dp, md = 6.dp, lg = 10.dp, xl = 14.dp)
  }
}
