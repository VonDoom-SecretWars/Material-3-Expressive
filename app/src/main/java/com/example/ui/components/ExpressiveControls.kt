package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ClockTab
import com.example.model.RingingAlert
import com.example.ui.theme.LocalExpressiveTokens

@Composable
fun FloatingBottomToolbar(
  selectedTab: ClockTab,
  onSelectTab: (ClockTab) -> Unit,
  onOpenThemeStudio: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current

  Row(
    modifier = modifier
      .windowInsetsPadding(WindowInsets.navigationBars)
      .padding(horizontal = 12.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Surface(
      shape = CircleShape,
      color = colors.surfaceContainerHigh.copy(alpha = 0.96f),
      border = BorderStroke(1.dp, colors.outlineVariant),
      shadowElevation = 12.dp,
    ) {
      Row(
        modifier = Modifier.padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        ClockTab.entries.forEach { tab ->
          val active = tab == selectedTab
          val bgColor by animateColorAsState(
            targetValue = if (active) colors.primaryContainer else Color.Transparent,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "nav_pill_bg",
          )
          val contentColor by animateColorAsState(
            targetValue = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant,
            label = "nav_pill_fg",
          )
          val icon = when (tab) {
            ClockTab.CLOCK -> if (active) Icons.Rounded.Schedule else Icons.Outlined.Schedule
            ClockTab.ALARMS -> if (active) Icons.Rounded.Alarm else Icons.Outlined.Alarm
            ClockTab.TIMER -> if (active) Icons.Rounded.HourglassBottom else Icons.Outlined.HourglassBottom
            ClockTab.STOPWATCH -> if (active) Icons.Rounded.Timer else Icons.Outlined.Timer
          }

          Row(
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .clip(CircleShape)
              .background(bgColor)
              .clickable(role = Role.Tab) { onSelectTab(tab) }
              .padding(horizontal = if (active) 16.dp else 12.dp, vertical = 10.dp)
              .testTag("nav_tab_${tab.name.lowercase()}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = icon,
              contentDescription = tab.label,
              tint = contentColor,
              modifier = Modifier.size(22.dp),
            )
            AnimatedVisibility(visible = active) {
              Text(
                text = tab.label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1,
              )
            }
          }
        }
      }
    }

    // Separate Expressive Brush FAB
    Surface(
      onClick = onOpenThemeStudio,
      shape = RoundedCornerShape(tokens.radii.md),
      color = colors.tertiaryContainer,
      contentColor = colors.onTertiaryContainer,
      shadowElevation = 12.dp,
      modifier = Modifier
        .size(56.dp)
        .testTag("open_theme_studio_fab"),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = Icons.Rounded.Brush,
          contentDescription = stringResource(R.string.cd_open_theme_studio),
          modifier = Modifier.size(24.dp),
        )
      }
    }
  }
}

@Composable
fun <T> ConnectedButtonGroup(
  items: List<T>,
  selectedItem: T,
  labelFor: (T) -> String,
  onSelect: (T) -> Unit,
  modifier: Modifier = Modifier,
  scrollable: Boolean = false,
) {
  val colors = MaterialTheme.colorScheme
  val view = LocalView.current
  val rowModifier = modifier
    .clip(CircleShape)
    .background(colors.surfaceContainerHighest)
    .padding(4.dp)

  Row(
    modifier = if (scrollable) rowModifier.horizontalScroll(rememberScrollState()) else rowModifier,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    items.forEachIndexed { index, item ->
      val isSelected = item == selectedItem
      val bg by animateColorAsState(
        targetValue = if (isSelected) colors.primaryContainer else Color.Transparent,
        label = "connected_bg_$index",
      )
      val fg by animateColorAsState(
        targetValue = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant,
        label = "connected_fg_$index",
      )
      val shape = when {
        isSelected -> CircleShape
        index == 0 -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp, topEnd = 12.dp, bottomEnd = 12.dp)
        index == items.lastIndex -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 50.dp, bottomEnd = 50.dp)
        else -> RoundedCornerShape(12.dp)
      }

      Box(
        modifier = Modifier
          .then(if (!scrollable) Modifier.weight(1f) else Modifier)
          .minimumInteractiveComponentSize()
          .clip(shape)
          .background(bg)
          .clickable {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
            onSelect(item)
          }
          .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = labelFor(item),
          style = MaterialTheme.typography.labelLarge,
          color = fg,
          maxLines = 1,
        )
      }
    }
  }
}

@Composable
fun MorphingPlayButton(
  isRunning: Boolean,
  idleLabel: String,
  runningLabel: String = stringResource(R.string.action_pause),
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current

  val cornerRadius by animateDpAsState(
    targetValue = if (isRunning) tokens.radii.md else 32.dp,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow,
    ),
    label = "morph_btn_corner",
  )
  val targetWidth by animateDpAsState(
    targetValue = if (isRunning) 152.dp else 140.dp,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "morph_btn_width",
  )
  val containerColor by animateColorAsState(
    targetValue = if (isRunning) colors.tertiary else colors.primary,
    label = "morph_btn_bg",
  )
  val contentColor by animateColorAsState(
    targetValue = if (isRunning) colors.onTertiary else colors.onPrimary,
    label = "morph_btn_fg",
  )

  Button(
    onClick = onClick,
    shape = RoundedCornerShape(cornerRadius),
    colors = ButtonDefaults.buttonColors(
      containerColor = containerColor,
      contentColor = contentColor,
    ),
    contentPadding = PaddingValues(horizontal = 24.dp),
    modifier = modifier
      .height(62.dp)
      .width(targetWidth),
  ) {
    Icon(
      imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
      contentDescription = null,
      modifier = Modifier.size(24.dp),
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = if (isRunning) runningLabel else idleLabel,
      style = MaterialTheme.typography.titleMedium.copy(
        fontFamily = tokens.displayFamily,
        fontWeight = FontWeight.ExtraBold,
      ),
    )
  }
}

@Composable
fun ExpressiveSwitch(
  checked: Boolean,
  onCheckedChange: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = MaterialTheme.colorScheme
  val haptic = LocalHapticFeedback.current
  val view = LocalView.current
  val trackColor by animateColorAsState(
    targetValue = if (checked) colors.primary else colors.surfaceContainerHighest,
    label = "switch_track",
  )
  val borderColor by animateColorAsState(
    targetValue = if (checked) colors.primary else colors.outline,
    label = "switch_border",
  )
  val thumbColor by animateColorAsState(
    targetValue = if (checked) colors.onPrimary else colors.outline,
    label = "switch_thumb",
  )
  val thumbOffset by animateDpAsState(
    targetValue = if (checked) 24.dp else 4.dp,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium,
    ),
    label = "switch_offset",
  )
  val thumbSize by animateDpAsState(
    targetValue = if (checked) 24.dp else 18.dp,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "switch_size",
  )

  Box(
    modifier = modifier
      .minimumInteractiveComponentSize()
      .width(54.dp)
      .height(34.dp)
      .clip(CircleShape)
      .background(trackColor)
      .border(width = 2.dp, color = borderColor, shape = CircleShape)
      .clickable(role = Role.Switch) {
        view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onCheckedChange()
      },
    contentAlignment = Alignment.CenterStart,
  ) {
    Box(
      modifier = Modifier
        .offset(x = thumbOffset)
        .size(thumbSize)
        .clip(CircleShape)
        .background(thumbColor),
      contentAlignment = Alignment.Center,
    ) {
      if (checked) {
        Icon(
          imageVector = Icons.Rounded.Check,
          contentDescription = null,
          tint = colors.primary,
          modifier = Modifier.size(14.dp),
        )
      }
    }
  }
}

@Composable
fun ExpressiveIconButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tonal: Boolean = false,
) {
  val colors = MaterialTheme.colorScheme
  Surface(
    onClick = onClick,
    shape = CircleShape,
    color = if (tonal) colors.primaryContainer else colors.surfaceContainer,
    contentColor = if (tonal) colors.onPrimaryContainer else colors.onSurface,
    border = if (tonal) null else BorderStroke(1.dp, colors.outlineVariant),
    modifier = modifier.size(48.dp),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = Modifier.size(22.dp),
      )
    }
  }
}

@Composable
fun RingingBannerOverlay(
  alert: RingingAlert?,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = MaterialTheme.colorScheme
  AnimatedVisibility(
    visible = alert != null,
    enter = slideInVertically(initialOffsetY = { -it * 2 }) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { -it * 2 }) + fadeOut(),
    modifier = modifier
      .windowInsetsPadding(WindowInsets.statusBars)
      .padding(horizontal = 16.dp, vertical = 12.dp),
  ) {
    if (alert != null) {
      Surface(
        shape = CircleShape,
        color = colors.tertiaryContainer,
        contentColor = colors.onTertiaryContainer,
        shadowElevation = 16.dp,
        modifier = Modifier
          .widthIn(max = 460.dp)
          .fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.NotificationsActive,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = alert.title,
              style = MaterialTheme.typography.titleMedium,
              maxLines = 1,
            )
            Text(
              text = alert.subtitle,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.onTertiaryContainer.copy(alpha = 0.85f),
            )
          }
          Button(
            onClick = onDismiss,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = colors.primary,
              contentColor = colors.onPrimary,
            ),
            modifier = Modifier.testTag("dismiss_ringing_button"),
          ) {
            Text(stringResource(R.string.dismiss))
          }
        }
      }
    }
  }
}
