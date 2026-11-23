package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.ClockTab
import com.example.model.SurfaceMode
import com.example.ui.components.ExpressiveIconButton
import com.example.ui.components.FloatingBottomToolbar
import com.example.ui.components.RingingBannerOverlay
import com.example.ui.screens.AlarmsTabScreen
import com.example.ui.screens.ClockTabScreen
import com.example.ui.screens.StandByScreen
import com.example.ui.screens.StopwatchTabScreen
import com.example.ui.screens.ThemeStudioSheet
import com.example.ui.screens.TimerTabScreen
import com.example.ui.theme.ChronosTheme
import com.example.viewmodel.ChronosViewModel
import java.util.Calendar

@Composable
fun ChronosApp(viewModel: ChronosViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  ChronosTheme(config = uiState.theme) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isAmbientMode) {
          StandByScreen(
            nowMillis = uiState.nowMillis,
            theme = uiState.theme,
            onTapDialToMorph = viewModel::cycleNextShape,
            onExitStandBy = { viewModel.setAmbientMode(false) },
          )
        } else {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .windowInsetsPadding(WindowInsets.safeDrawing)
              .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Column(
              modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize(),
              verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
              ChronosTopBar(
                nowMillis = uiState.nowMillis,
                surfaceMode = uiState.theme.surface,
                onCycleSurface = viewModel::cycleSurfaceMode,
                onEnterStandBy = viewModel::toggleAmbientMode,
                onOpenThemeStudio = { viewModel.setThemeSheetOpen(true) },
              )

              AnimatedContent(
                targetState = uiState.selectedTab,
                transitionSpec = {
                  (fadeIn() + slideInVertically { it / 24 }) togetherWith fadeOut()
                },
                label = "tab_content",
                modifier = Modifier.weight(1f),
              ) { tab ->
                when (tab) {
                  ClockTab.CLOCK -> ClockTabScreen(
                    nowMillis = uiState.nowMillis,
                    theme = uiState.theme,
                    worldCities = uiState.worldCities,
                    onTapDialToMorph = viewModel::cycleNextShape,
                    onAddCity = viewModel::addWorldCity,
                    onRemoveCity = viewModel::removeWorldCity,
                  )

                  ClockTab.ALARMS -> AlarmsTabScreen(
                    alarms = uiState.alarms,
                    use24h = uiState.theme.use24h,
                    onTestSound = viewModel::playTestSound,
                    onAddAlarm = viewModel::addAlarm,
                    onToggleAlarm = viewModel::toggleAlarm,
                    onDeleteAlarm = viewModel::deleteAlarm,
                  )

                  ClockTab.TIMER -> TimerTabScreen(
                    totalMs = uiState.timerTotalMs,
                    remainMs = uiState.timerRemainMs,
                    isRunning = uiState.timerRunning,
                    wavePhase = uiState.timerWavePhase,
                    onSelectPreset = viewModel::setTimerPreset,
                    onToggle = viewModel::toggleTimer,
                    onReset = viewModel::resetTimer,
                    onAddMinute = viewModel::addTimerMinute,
                  )

                  ClockTab.STOPWATCH -> StopwatchTabScreen(
                    elapsedMs = uiState.stopwatchElapsedMs,
                    isRunning = uiState.stopwatchRunning,
                    laps = uiState.stopwatchLaps,
                    onToggle = viewModel::toggleStopwatch,
                    onReset = viewModel::resetStopwatch,
                    onRecordLap = viewModel::recordLap,
                  )
                }
              }
            }
          }

          FloatingBottomToolbar(
            selectedTab = uiState.selectedTab,
            onSelectTab = viewModel::selectTab,
            onOpenThemeStudio = { viewModel.setThemeSheetOpen(true) },
            modifier = Modifier.align(Alignment.BottomCenter),
          )
        }

        RingingBannerOverlay(
          alert = uiState.ringingAlert,
          onDismiss = viewModel::dismissRinging,
          modifier = Modifier.align(Alignment.TopCenter),
        )

        if (uiState.isThemeSheetOpen) {
          ThemeStudioSheet(
            theme = uiState.theme,
            onDismiss = { viewModel.setThemeSheetOpen(false) },
            onApplyPreset = viewModel::applyPreset,
            onSelectSeedColor = viewModel::setSeedColor,
            onSelectScheme = viewModel::setPaletteScheme,
            onSelectSurface = viewModel::setSurfaceMode,
            onSelectShape = viewModel::setDialShape,
            onSelectLayout = viewModel::setClockLayout,
            onSelectHandStyle = viewModel::setHandStyle,
            onSelectSweepMode = viewModel::setSweepMode,
            onSelectFont = viewModel::setFontPersonality,
            onChangeWeight = viewModel::setClockWeight,
            onSelectRoundness = viewModel::setCornerStyle,
            onToggle24h = viewModel::toggleUse24h,
            onToggleSeconds = viewModel::toggleShowSeconds,
            onToggleNumbers = viewModel::toggleShowNumbers,
            onExportJson = viewModel::exportThemeJson,
            onImportJson = viewModel::importThemeJson,
            onReset = viewModel::resetTheme,
          )
        }
      }
    }
  }
}

@Composable
private fun ChronosTopBar(
  nowMillis: Long,
  surfaceMode: SurfaceMode,
  onCycleSurface: () -> Unit,
  onEnterStandBy: () -> Unit,
  onOpenThemeStudio: () -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  val hour = remember(nowMillis / 60_000L) {
    Calendar.getInstance().apply { timeInMillis = nowMillis }.get(Calendar.HOUR_OF_DAY)
  }
  val greeting = when {
    hour < 12 -> stringResource(R.string.greeting_morning)
    hour < 17 -> stringResource(R.string.greeting_afternoon)
    else -> stringResource(R.string.greeting_evening)
  }
  val surfaceIcon = when (surfaceMode) {
    SurfaceMode.LIGHT -> Icons.Rounded.LightMode
    SurfaceMode.AMOLED -> Icons.Rounded.Contrast
    SurfaceMode.DARK -> Icons.Rounded.DarkMode
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.AutoAwesome,
          contentDescription = null,
          tint = colors.primary,
          modifier = Modifier.size(14.dp),
        )
        Text(
          text = stringResource(R.string.brand_eyebrow).uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = colors.primary,
        )
      }
      Text(
        text = greeting,
        style = MaterialTheme.typography.headlineLarge,
        color = colors.onSurface,
      )
    }

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      ExpressiveIconButton(
        icon = surfaceIcon,
        contentDescription = stringResource(R.string.cd_toggle_surface),
        onClick = onCycleSurface,
        modifier = Modifier.testTag("cycle_surface_button"),
      )
      ExpressiveIconButton(
        icon = Icons.Rounded.OpenInFull,
        contentDescription = stringResource(R.string.cd_standby_mode),
        onClick = onEnterStandBy,
        modifier = Modifier.testTag("enter_standby_button"),
      )
      ExpressiveIconButton(
        icon = Icons.Rounded.Palette,
        contentDescription = stringResource(R.string.cd_open_theme_studio),
        onClick = onOpenThemeStudio,
        tonal = true,
        modifier = Modifier.testTag("top_theme_studio_button"),
      )
    }
  }
}
