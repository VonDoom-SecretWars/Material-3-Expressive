package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.StopwatchLap
import com.example.ui.components.ExpressiveIconButton
import com.example.ui.components.MorphingPlayButton
import com.example.ui.theme.LocalExpressiveTokens
import java.util.Locale

@Composable
fun StopwatchTabScreen(
  elapsedMs: Long,
  isRunning: Boolean,
  laps: List<StopwatchLap>,
  onToggle: () -> Unit,
  onReset: () -> Unit,
  onRecordLap: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current

  val (mainText, centisText) = formatStopwatchParts(elapsedMs)
  val idleLabel = if (elapsedMs > 0L) stringResource(R.string.action_resume) else stringResource(R.string.action_start)

  val (minSplit, maxSplit) = remember(laps) {
    if (laps.size > 1) {
      laps.minOf { it.splitMs } to laps.maxOf { it.splitMs }
    } else {
      -1L to -1L
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 112.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(tokens.radii.xl),
      color = colors.surfaceContainerLow,
      border = BorderStroke(1.dp, colors.outlineVariant),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(26.dp),
      ) {
        Row(
          verticalAlignment = Alignment.Bottom,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = mainText,
            style = MaterialTheme.typography.displayLarge.copy(
              fontFamily = tokens.displayFamily,
              fontWeight = tokens.clockFontWeight,
              fontSize = 62.sp,
              fontFeatureSettings = "tnum",
            ),
            color = colors.onSurface,
          )
          Text(
            text = centisText,
            style = MaterialTheme.typography.headlineLarge.copy(
              fontFamily = tokens.monoFamily,
              fontWeight = FontWeight.Bold,
              fontFeatureSettings = "tnum",
            ),
            color = colors.primary,
            modifier = Modifier.padding(bottom = 8.dp),
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          ExpressiveIconButton(
            icon = Icons.Rounded.RestartAlt,
            contentDescription = stringResource(R.string.cd_reset_stopwatch),
            onClick = onReset,
            modifier = Modifier.testTag("stopwatch_reset_button"),
          )

          MorphingPlayButton(
            isRunning = isRunning,
            idleLabel = idleLabel,
            onClick = onToggle,
            modifier = Modifier.testTag("stopwatch_toggle_button"),
          )

          Button(
            onClick = onRecordLap,
            enabled = isRunning || elapsedMs > 0L,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = colors.secondaryContainer,
              contentColor = colors.onSecondaryContainer,
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            modifier = Modifier.testTag("stopwatch_lap_button"),
          ) {
            Icon(Icons.Rounded.Flag, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.action_lap))
          }
        }
      }
    }

    if (laps.isEmpty()) {
      Surface(
        shape = RoundedCornerShape(tokens.radii.lg),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = stringResource(R.string.empty_laps),
          style = MaterialTheme.typography.bodyMedium,
          color = colors.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(20.dp),
        )
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        laps.forEach { lap ->
          val splitFormatted = formatStopwatchParts(lap.splitMs).let { "${it.first}${it.second}" }
          val totalFormatted = formatStopwatchParts(lap.totalMs).let { "${it.first}${it.second}" }
          val isFastest = laps.size > 1 && lap.splitMs == minSplit
          val isSlowest = laps.size > 1 && lap.splitMs == maxSplit

          Surface(
            shape = RoundedCornerShape(tokens.radii.md),
            color = colors.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Text(
                  text = "Lap ${lap.number}",
                  style = MaterialTheme.typography.titleMedium,
                  color = colors.primary,
                )
                if (isFastest) {
                  LapBadge(
                    label = stringResource(R.string.fastest),
                    container = colors.tertiaryContainer,
                    content = colors.onTertiaryContainer,
                  )
                } else if (isSlowest) {
                  LapBadge(
                    label = stringResource(R.string.slowest),
                    container = colors.secondaryContainer,
                    content = colors.onSecondaryContainer,
                  )
                }
              }

              Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                  text = "+$splitFormatted",
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = tokens.monoFamily,
                    fontFeatureSettings = "tnum",
                  ),
                  color = colors.onSurface,
                )
                Text(
                  text = totalFormatted,
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = tokens.monoFamily,
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum",
                  ),
                  color = colors.onSurfaceVariant,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun LapBadge(
  label: String,
  container: androidx.compose.ui.graphics.Color,
  content: androidx.compose.ui.graphics.Color,
) {
  Surface(
    shape = CircleShape,
    color = container,
    contentColor = content,
  ) {
    Text(
      text = label.uppercase(),
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
    )
  }
}

private fun formatStopwatchParts(ms: Long): Pair<String, String> {
  val totalSec = ms / 1000L
  val mins = totalSec / 60L
  val secs = totalSec % 60L
  val centis = (ms % 1000L) / 10L
  return String.format(Locale.US, "%02d:%02d", mins, secs) to String.format(Locale.US, ".%02d", centis)
}
