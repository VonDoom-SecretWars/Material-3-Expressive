package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAlarm
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.AlarmItem
import com.example.ui.components.ExpressiveSwitch
import com.example.ui.theme.LocalExpressiveTokens
import java.util.Locale

private val DAY_LABELS = listOf("S", "M", "T", "W", "T", "F", "S")

@Composable
fun AlarmsTabScreen(
  alarms: List<AlarmItem>,
  use24h: Boolean,
  onTestSound: () -> Unit,
  onAddAlarm: (hour: Int, minute: Int, label: String, days: List<Int>) -> Unit,
  onToggleAlarm: (Long) -> Unit,
  onDeleteAlarm: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current
  var showCreateDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 112.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(R.string.alarms_title),
        style = MaterialTheme.typography.titleLarge,
        color = colors.onSurface,
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = onTestSound,
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.secondaryContainer,
            contentColor = colors.onSecondaryContainer,
          ),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
          modifier = Modifier.testTag("test_alarm_sound_button"),
        ) {
          Icon(Icons.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(6.dp))
          Text(stringResource(R.string.test_sound))
        }

        Button(
          onClick = { showCreateDialog = true },
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
          modifier = Modifier.testTag("new_alarm_button"),
        ) {
          Icon(Icons.Rounded.AddAlarm, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(6.dp))
          Text(stringResource(R.string.new_alarm))
        }
      }
    }

    if (alarms.isEmpty()) {
      Surface(
        shape = RoundedCornerShape(tokens.radii.lg),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(28.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Alarm,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(36.dp),
          )
          Text(
            text = stringResource(R.string.empty_alarms),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
          )
        }
      }
    } else {
      BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 640.dp) 2 else 1
        val rows = alarms.chunked(columns)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          rows.forEach { rowAlarms ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              rowAlarms.forEach { alarm ->
                AlarmCard(
                  alarm = alarm,
                  use24h = use24h,
                  onToggle = { onToggleAlarm(alarm.id) },
                  onDelete = { onDeleteAlarm(alarm.id) },
                  modifier = Modifier.weight(1f),
                )
              }
              if (rowAlarms.size < columns) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }
      }
    }
  }

  if (showCreateDialog) {
    CreateAlarmDialog(
      use24h = use24h,
      onSave = { h, m, label, days ->
        onAddAlarm(h, m, label, days)
        showCreateDialog = false
      },
      onDismiss = { showCreateDialog = false },
    )
  }
}

@Composable
private fun AlarmCard(
  alarm: AlarmItem,
  use24h: Boolean,
  onToggle: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current

  val hourStr = if (use24h) {
    String.format(Locale.US, "%02d", alarm.hour)
  } else {
    val h12 = alarm.hour % 12
    (if (h12 == 0) 12 else h12).toString()
  }
  val minStr = String.format(Locale.US, "%02d", alarm.minute)
  val amPmStr = if (alarm.hour >= 12) "PM" else "AM"

  Surface(
    shape = RoundedCornerShape(tokens.radii.lg),
    color = if (alarm.enabled) colors.surfaceContainerHigh else colors.surfaceContainer,
    border = BorderStroke(
      width = 1.dp,
      color = if (alarm.enabled) colors.primaryContainer else colors.outlineVariant,
    ),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.Bottom,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.alpha(if (alarm.enabled) 1f else 0.52f),
        ) {
          Text(
            text = "$hourStr:$minStr",
            style = MaterialTheme.typography.displayMedium.copy(
              fontFamily = tokens.displayFamily,
              fontWeight = tokens.clockFontWeight,
              fontSize = 36.sp,
              fontFeatureSettings = "tnum",
            ),
            color = colors.onSurface,
          )
          if (!use24h) {
            Text(
              text = amPmStr,
              style = MaterialTheme.typography.titleMedium,
              color = colors.primary,
              modifier = Modifier.padding(bottom = 4.dp),
            )
          }
        }

        ExpressiveSwitch(
          checked = alarm.enabled,
          onCheckedChange = onToggle,
          modifier = Modifier.testTag("alarm_switch_${alarm.id}"),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = alarm.label.ifBlank { "Alarm" },
          style = MaterialTheme.typography.bodyLarge,
          color = colors.onSurfaceVariant,
          modifier = Modifier.weight(1f),
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          DAY_LABELS.forEachIndexed { idx, letter ->
            val active = idx in alarm.days
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (active) colors.primaryContainer else colors.surfaceContainerHighest),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = letter,
                style = MaterialTheme.typography.labelSmall,
                color = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant,
              )
            }
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag("delete_alarm_${alarm.id}"),
          ) {
            Icon(
              imageVector = Icons.Rounded.Delete,
              contentDescription = stringResource(R.string.cd_delete_alarm),
              tint = colors.onSurfaceVariant,
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CreateAlarmDialog(
  use24h: Boolean,
  onSave: (hour: Int, minute: Int, label: String, days: List<Int>) -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current

  var hourText by remember { mutableStateOf("07") }
  var minuteText by remember { mutableStateOf("30") }
  var isPm by remember { mutableStateOf(false) }
  var labelText by remember { mutableStateOf("") }
  val selectedDays = remember { mutableStateListOf(1, 2, 3, 4, 5) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(tokens.radii.xl),
      color = colors.surfaceContainerHigh,
      border = BorderStroke(1.dp, colors.outlineVariant),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        Text(
          text = stringResource(R.string.set_new_alarm),
          style = MaterialTheme.typography.titleLarge,
          color = colors.onSurface,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          OutlinedTextField(
            value = hourText,
            onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) hourText = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.headlineLarge.copy(
              fontFamily = tokens.displayFamily,
              textAlign = TextAlign.Center,
            ),
            shape = RoundedCornerShape(tokens.radii.md),
            modifier = Modifier.width(86.dp),
          )
          Text(
            text = ":",
            style = MaterialTheme.typography.headlineLarge,
            color = colors.onSurface,
          )
          OutlinedTextField(
            value = minuteText,
            onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) minuteText = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.headlineLarge.copy(
              fontFamily = tokens.displayFamily,
              textAlign = TextAlign.Center,
            ),
            shape = RoundedCornerShape(tokens.radii.md),
            modifier = Modifier.width(86.dp),
          )

          if (!use24h) {
            Button(
              onClick = { isPm = !isPm },
              shape = RoundedCornerShape(tokens.radii.md),
              colors = ButtonDefaults.buttonColors(
                containerColor = colors.secondaryContainer,
                contentColor = colors.onSecondaryContainer,
              ),
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
            ) {
              Text(
                text = if (isPm) "PM" else "AM",
                style = MaterialTheme.typography.titleMedium,
              )
            }
          }
        }

        OutlinedTextField(
          value = labelText,
          onValueChange = { labelText = it },
          placeholder = { Text(stringResource(R.string.alarm_label_hint)) },
          singleLine = true,
          shape = RoundedCornerShape(tokens.radii.md),
          modifier = Modifier.fillMaxWidth(),
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = stringResource(R.string.repeat_days),
            style = MaterialTheme.typography.labelSmall,
            color = colors.primary,
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(CircleShape)
              .background(colors.surfaceContainerHighest)
              .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            DAY_LABELS.forEachIndexed { idx, letter ->
              val active = idx in selectedDays
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(CircleShape)
                  .background(if (active) colors.primaryContainer else colors.surfaceContainerHighest)
                  .clickable {
                    if (active) selectedDays.remove(idx) else selectedDays.add(idx)
                  }
                  .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = letter,
                  style = MaterialTheme.typography.labelLarge,
                  color = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant,
                )
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        ) {
          OutlinedButton(onClick = onDismiss, shape = CircleShape) {
            Text(stringResource(R.string.cancel))
          }
          Button(
            onClick = {
              val rawHour = hourText.toIntOrNull() ?: 7
              val rawMin = (minuteText.toIntOrNull() ?: 0).coerceIn(0, 59)
              val finalHour = if (use24h) {
                rawHour.coerceIn(0, 23)
              } else {
                val h12 = rawHour.coerceIn(1, 12)
                when {
                  isPm && h12 < 12 -> h12 + 12
                  !isPm && h12 == 12 -> 0
                  else -> h12
                }
              }
              onSave(finalHour, rawMin, labelText, selectedDays.toList())
            },
            shape = CircleShape,
            modifier = Modifier.testTag("save_alarm_button"),
          ) {
            Text(stringResource(R.string.save_alarm))
          }
        }
      }
    }
  }
}
