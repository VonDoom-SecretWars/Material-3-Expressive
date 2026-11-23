package com.example.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import com.example.model.ClockThemeConfig
import com.example.model.SurfaceMode

data class ExpressiveThemeTokens(
  val radii: ExpressiveRadii,
  val displayFamily: FontFamily,
  val clockFontWeight: FontWeight,
  val monoFamily: FontFamily = JetBrainsMonoFamily,
  val isDark: Boolean = true,
)

val LocalExpressiveTokens = staticCompositionLocalOf {
  ExpressiveThemeTokens(
    radii = DynamicColorEngine.radiiFor(ClockThemeConfig().roundness),
    displayFamily = RobotoFlexFamily,
    clockFontWeight = FontWeight.Bold,
    isDark = true,
  )
}

@Composable
private fun animateColor(target: Color): Color {
  val animated by animateColorAsState(
    targetValue = target,
    animationSpec = tween(durationMillis = 320),
    label = "theme_color",
  )
  return animated
}

@Composable
private fun animatedColorScheme(target: ColorScheme): ColorScheme {
  return target.copy(
    primary = animateColor(target.primary),
    onPrimary = animateColor(target.onPrimary),
    primaryContainer = animateColor(target.primaryContainer),
    onPrimaryContainer = animateColor(target.onPrimaryContainer),
    secondary = animateColor(target.secondary),
    onSecondary = animateColor(target.onSecondary),
    secondaryContainer = animateColor(target.secondaryContainer),
    onSecondaryContainer = animateColor(target.onSecondaryContainer),
    tertiary = animateColor(target.tertiary),
    onTertiary = animateColor(target.onTertiary),
    tertiaryContainer = animateColor(target.tertiaryContainer),
    onTertiaryContainer = animateColor(target.onTertiaryContainer),
    background = animateColor(target.background),
    onBackground = animateColor(target.onBackground),
    surface = animateColor(target.surface),
    onSurface = animateColor(target.onSurface),
    surfaceVariant = animateColor(target.surfaceVariant),
    onSurfaceVariant = animateColor(target.onSurfaceVariant),
    surfaceContainerLowest = animateColor(target.surfaceContainerLowest),
    surfaceContainerLow = animateColor(target.surfaceContainerLow),
    surfaceContainer = animateColor(target.surfaceContainer),
    surfaceContainerHigh = animateColor(target.surfaceContainerHigh),
    surfaceContainerHighest = animateColor(target.surfaceContainerHighest),
    outline = animateColor(target.outline),
    outlineVariant = animateColor(target.outlineVariant),
  )
}

@Composable
fun ChronosTheme(
  config: ClockThemeConfig = ClockThemeConfig(),
  content: @Composable () -> Unit,
) {
  val rawScheme = remember(config.seedColorHex, config.scheme, config.surface) {
    DynamicColorEngine.generateColorScheme(config)
  }
  val colorScheme = animatedColorScheme(rawScheme)
  val isDark = config.surface != SurfaceMode.LIGHT

  val displayFamily = remember(config.font) { displayFontFamilyFor(config.font) }
  val radii = remember(config.roundness) { DynamicColorEngine.radiiFor(config.roundness) }
  val clockWeight = remember(config.clockWeight) { clockFontWeightFor(config.clockWeight) }

  val tokens = remember(radii, displayFamily, clockWeight, isDark) {
    ExpressiveThemeTokens(
      radii = radii,
      displayFamily = displayFamily,
      clockFontWeight = clockWeight,
      isDark = isDark,
    )
  }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        WindowCompat.getInsetsController(window, view).apply {
          isAppearanceLightStatusBars = !isDark
          isAppearanceLightNavigationBars = !isDark
        }
      }
    }
  }

  CompositionLocalProvider(LocalExpressiveTokens provides tokens) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = remember(displayFamily) { chronosTypography(displayFamily) },
      content = content,
    )
  }
}
