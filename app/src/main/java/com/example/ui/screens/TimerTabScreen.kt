package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ClockCatalogs
import com.example.ui.components.ConnectedButtonGroup
import com.example.ui.components.ExpressiveIconButton
import com.example.ui.components.MorphingPlayButton
import com.example.ui.components.WavyProgressRing
import com.example.ui.theme.LocalExpressiveTokens
import java.util.Locale
import kotlin.math.ceil

@Composable
fun TimerTabScreen(
  totalMs: Long,
  remainMs: Long,
  isRunning: Boolean,
  wavePhase: Float,
  onSelectPreset: (Int) -> Unit,
  onToggle: () -> Unit,
  onReset: () -> Unit,
  onAddMinute: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current

  val totalSec = ceil(remainMs / 1000.0).toLong().coerceAtLeast(0L)
  val mins = totalSec / 60L
  val secs = totalSec % 60L
  val timeFormatted = String.format(Locale.US, "%02d:%02d", mins, secs)

  val progress = if (totalMs > 0L) (remainMs.toFloat() / totalMs.toFloat()) else 0f
  val selectedPresetSeconds = (totalMs / 1000L).toInt()
  val statusLabel = when {
    isRunning -> stringResource(R.string.timer_running)
    remainMs == 0L -> stringResource(R.string.timer_finished)
    remainMs < totalMs -> stringResource(R.string.timer_paused)
    else -> stringResource(R.string.timer_ready)
  }
  val playIdleLabel = if (remainMs in 1L until totalMs) {
    stringResource(R.string.action_resume)
  } else {
    stringResource(R.string.action_start)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 112.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(tokens.radii.xl),
      color = colors.surfaceContainerLow,
      border = BorderStroke(1.dp, colors.outlineVariant),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        WavyProgressRing(
          progress = progress,
          wavePhase = wavePhase,
          isRunning = isRunning,
          size = 264.dp,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = timeFormatted,
              style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = tokens.displayFamily,
                fontWeight = tokens.clockFontWeight,
                fontSize = 48.sp,
                fontFeatureSettings = "tnum",
              ),
              color = colors.onSurface,
            )
            Text(
              text = statusLabel.uppercase(),
              style = MaterialTheme.typography.labelSmall,
              color = colors.primary,
            )
          }
        }

        ConnectedButtonGroup(
          items = ClockCatalogs.TIMER_PRESETS_SECONDS,
          selectedItem = selectedPresetSeconds,
          labelFor = { "${it / 60}m" },
          onSelect = onSelectPreset,
          scrollable = true,
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          ExpressiveIconButton(
            icon = Icons.Rounded.RestartAlt,
            contentDescription = stringResource(R.string.cd_reset_timer),
            onClick = onReset,
            modifier = Modifier.testTag("timer_reset_button"),
          )

          MorphingPlayButton(
            isRunning = isRunning,
            idleLabel = playIdleLabel,
            onClick = onToggle,
            modifier = Modifier.testTag("timer_toggle_button"),
          )

          Button(
            onClick = onAddMinute,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = colors.secondaryContainer,
              contentColor = colors.onSecondaryContainer,
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            modifier = Modifier.testTag("timer_add_minute_button"),
          ) {
            Text(
              text = stringResource(R.string.add_one_minute),
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            )
          }
        }
      }
    }
  }
}
