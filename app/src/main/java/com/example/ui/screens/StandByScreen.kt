package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloseFullscreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ClockThemeConfig
import com.example.ui.components.ExpressiveIconButton

@Composable
fun StandByScreen(
  nowMillis: Long,
  theme: ClockThemeConfig,
  onTapDialToMorph: () -> Unit,
  onExitStandBy: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onExitStandBy)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.surface)
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(20.dp),
  ) {
    HeroClockCard(
      nowMillis = nowMillis,
      theme = theme,
      onTapDialToMorph = onTapDialToMorph,
      isAmbient = true,
      modifier = Modifier
        .widthIn(max = 820.dp)
        .align(Alignment.Center),
    )

    ExpressiveIconButton(
      icon = Icons.Rounded.CloseFullscreen,
      contentDescription = stringResource(R.string.cd_exit_standby),
      onClick = onExitStandBy,
      modifier = Modifier
        .align(Alignment.TopEnd)
        .testTag("exit_standby_button"),
    )
  }
}
