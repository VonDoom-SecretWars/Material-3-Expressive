package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.ClockCatalogs
import com.example.model.ClockLayout
import com.example.model.ClockThemeConfig
import com.example.model.CornerStyle
import com.example.model.CuratedPreset
import com.example.model.DialShape
import com.example.model.FontPersonality
import com.example.model.HandStyle
import com.example.model.PaletteScheme
import com.example.model.SurfaceMode
import com.example.model.SweepMode
import com.example.ui.components.ConnectedButtonGroup
import com.example.ui.components.ShapePreviewIcon
import com.example.ui.theme.DynamicColorEngine
import com.example.ui.theme.LocalExpressiveTokens
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ThemeStudioSheet(
  theme: ClockThemeConfig,
  onDismiss: () -> Unit,
  onApplyPreset: (CuratedPreset) -> Unit,
  onSelectSeedColor: (String) -> Unit,
  onSelectScheme: (PaletteScheme) -> Unit,
  onSelectSurface: (SurfaceMode) -> Unit,
  onSelectShape: (DialShape) -> Unit,
  onSelectLayout: (ClockLayout) -> Unit,
  onSelectHandStyle: (HandStyle) -> Unit,
  onSelectSweepMode: (SweepMode) -> Unit,
  onSelectFont: (FontPersonality) -> Unit,
  onChangeWeight: (Int) -> Unit,
  onSelectRoundness: (CornerStyle) -> Unit,
  onToggle24h: () -> Unit,
  onToggleSeconds: () -> Unit,
  onToggleNumbers: () -> Unit,
  onExportJson: () -> String,
  onImportJson: (String) -> Result<Unit>,
  onReset: () -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current
  val view = LocalView.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var showColorPicker by remember { mutableStateOf(false) }
  var jsonDialogMode by remember { mutableStateOf<String?>(null) } // "export" | "import"

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = colors.surfaceContainerLow,
    contentColor = colors.onSurface,
    shape = RoundedCornerShape(topStart = tokens.radii.xl, topEnd = tokens.radii.xl),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = stringResource(R.string.theme_studio_eyebrow).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.primary,
          )
          Text(
            text = stringResource(R.string.theme_studio_title),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurface,
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cd_close))
        }
      }

      HorizontalDivider(color = colors.outlineVariant)

      // 1. Curated Presets
      StudioSection(title = stringResource(R.string.studio_curated_presets)) {
        val rows = ClockCatalogs.CURATED_PRESETS.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          rows.forEach { pair ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              pair.forEach { preset ->
                Surface(
                  onClick = {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                    onApplyPreset(preset)
                  },
                  shape = RoundedCornerShape(tokens.radii.md),
                  color = colors.surfaceContainer,
                  border = BorderStroke(1.dp, colors.outlineVariant),
                  modifier = Modifier.weight(1f),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                  ) {
                    Box(
                      modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(DynamicColorEngine.parseHexColor(preset.seedColorHex))
                        .border(1.5.dp, colors.onSurface.copy(alpha = 0.25f), CircleShape),
                    )
                    Text(
                      text = preset.name,
                      style = MaterialTheme.typography.labelLarge,
                      maxLines = 1,
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 2. Dynamic Seed Color
      StudioSection(
        title = stringResource(R.string.studio_seed_color),
        trailing = theme.seedColorHex,
      ) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          ClockCatalogs.SEED_SWATCHES.forEach { hex ->
            val selected = hex.equals(theme.seedColorHex, ignoreCase = true)
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DynamicColorEngine.parseHexColor(hex))
                .border(
                  width = if (selected) 3.dp else 1.dp,
                  color = if (selected) colors.onSurface else colors.outlineVariant,
                  shape = CircleShape,
                )
                .clickable {
                  view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                  onSelectSeedColor(hex)
                },
            )
          }

          Surface(
            onClick = { showColorPicker = true },
            shape = CircleShape,
            color = colors.surfaceContainerHighest,
            border = BorderStroke(1.dp, colors.outline),
            modifier = Modifier.size(36.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Rounded.Colorize,
                contentDescription = "Custom seed color",
                tint = colors.onSurface,
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }
      }

      // 3. Palette Scheme
      StudioSection(title = stringResource(R.string.studio_palette_scheme)) {
        ConnectedButtonGroup(
          items = PaletteScheme.entries,
          selectedItem = theme.scheme,
          labelFor = { it.label },
          onSelect = onSelectScheme,
          modifier = Modifier.fillMaxWidth(),
        )
      }

      // 4. Surface Mode
      StudioSection(title = stringResource(R.string.studio_surface_mode)) {
        ConnectedButtonGroup(
          items = SurfaceMode.entries,
          selectedItem = theme.surface,
          labelFor = { it.label },
          onSelect = onSelectSurface,
          modifier = Modifier.fillMaxWidth(),
        )
      }

      // 5. Material Shape Dial
      StudioSection(title = stringResource(R.string.studio_material_shapes)) {
        val shapeRows = DialShape.entries.chunked(4)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          shapeRows.forEach { row ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              row.forEach { shape ->
                val active = shape == theme.shape
                Surface(
                  onClick = {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                    onSelectShape(shape)
                  },
                  shape = RoundedCornerShape(tokens.radii.md),
                  color = if (active) colors.primaryContainer else colors.surfaceContainer,
                  contentColor = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant,
                  border = BorderStroke(1.dp, if (active) colors.primary else colors.outlineVariant),
                  modifier = Modifier.weight(1f),
                ) {
                  Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    ShapePreviewIcon(
                      shape = shape,
                      tint = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant,
                      size = 30.dp,
                    )
                    Text(
                      text = shape.label,
                      style = MaterialTheme.typography.labelSmall,
                      maxLines = 1,
                    )
                  }
                }
              }
              repeat(4 - row.size) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }
      }

      // 6. Clock Face Layout
      StudioSection(title = stringResource(R.string.studio_clock_layout)) {
        ConnectedButtonGroup(
          items = ClockLayout.entries,
          selectedItem = theme.layout,
          labelFor = { it.label },
          onSelect = onSelectLayout,
          modifier = Modifier.fillMaxWidth(),
        )
      }

      // 7. Hand Style & Motion
      StudioSection(title = stringResource(R.string.studio_hand_motion)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          ConnectedButtonGroup(
            items = HandStyle.entries,
            selectedItem = theme.handStyle,
            labelFor = { it.label },
            onSelect = onSelectHandStyle,
            modifier = Modifier.fillMaxWidth(),
          )
          ConnectedButtonGroup(
            items = SweepMode.entries,
            selectedItem = theme.sweepMode,
            labelFor = { it.label },
            onSelect = onSelectSweepMode,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }

      // 8. Typography Personality & Weight
      StudioSection(title = stringResource(R.string.studio_typography)) {
        ConnectedButtonGroup(
          items = FontPersonality.entries,
          selectedItem = theme.font,
          labelFor = { it.label },
          onSelect = onSelectFont,
          modifier = Modifier.fillMaxWidth(),
        )
      }

      StudioSection(
        title = stringResource(R.string.studio_clock_weight),
        trailing = theme.clockWeight.toString(),
      ) {
        Slider(
          value = theme.clockWeight.toFloat(),
          onValueChange = { raw ->
            val snapped = ((raw / 50f).roundToInt() * 50).coerceIn(300, 900)
            onChangeWeight(snapped)
          },
          valueRange = 300f..900f,
          steps = 11,
        )
      }

      // 9. Container Corner Style
      StudioSection(title = stringResource(R.string.studio_corner_style)) {
        ConnectedButtonGroup(
          items = CornerStyle.entries,
          selectedItem = theme.roundness,
          labelFor = { it.label },
          onSelect = onSelectRoundness,
          modifier = Modifier.fillMaxWidth(),
        )
      }

      // 10. Clock Details
      StudioSection(title = stringResource(R.string.studio_clock_details)) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          DetailToggleChip(
            label = "24-Hour: ${if (theme.use24h) "On" else "Off"}",
            active = theme.use24h,
            onClick = {
              view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
              onToggle24h()
            },
          )
          DetailToggleChip(
            label = "Seconds: ${if (theme.showSeconds) "On" else "Off"}",
            active = theme.showSeconds,
            onClick = {
              view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
              onToggleSeconds()
            },
          )
          DetailToggleChip(
            label = "Dial Nums: ${if (theme.showNumbers) "On" else "Off"}",
            active = theme.showNumbers,
            onClick = {
              view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
              onToggleNumbers()
            },
          )
        }
      }

      HorizontalDivider(color = colors.outlineVariant)

      // 11. Export / Import / Reset
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Button(
          onClick = { jsonDialogMode = "export" },
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.secondaryContainer,
            contentColor = colors.onSecondaryContainer,
          ),
        ) {
          Text(stringResource(R.string.studio_export_json))
        }
        Button(
          onClick = { jsonDialogMode = "import" },
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.secondaryContainer,
            contentColor = colors.onSecondaryContainer,
          ),
        ) {
          Text(stringResource(R.string.studio_import_json))
        }
        OutlinedButton(
          onClick = onReset,
          shape = CircleShape,
          modifier = Modifier.testTag("reset_theme_button"),
        ) {
          Text(stringResource(R.string.studio_reset))
        }
      }
    }
  }

  if (showColorPicker) {
    CustomSeedColorDialog(
      initialHex = theme.seedColorHex,
      onApply = { hex ->
        onSelectSeedColor(hex)
        showColorPicker = false
      },
      onDismiss = { showColorPicker = false },
    )
  }

  if (jsonDialogMode != null) {
    ThemeJsonDialog(
      mode = jsonDialogMode!!,
      initialJson = if (jsonDialogMode == "export") onExportJson() else "",
      onImport = onImportJson,
      onDismiss = { jsonDialogMode = null },
    )
  }
}

@Composable
private fun StudioSection(
  title: String,
  trailing: String? = null,
  content: @Composable () -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = colors.primary,
      )
      if (trailing != null) {
        Text(
          text = trailing,
          style = MaterialTheme.typography.labelMedium.copy(fontFamily = tokens.monoFamily),
          color = colors.onSurfaceVariant,
        )
      }
    }
    content()
  }
}

@Composable
private fun DetailToggleChip(
  label: String,
  active: Boolean,
  onClick: () -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  Button(
    onClick = onClick,
    shape = CircleShape,
    colors = ButtonDefaults.buttonColors(
      containerColor = if (active) colors.primaryContainer else colors.surfaceContainerHighest,
      contentColor = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant,
    ),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
  ) {
    Text(label, style = MaterialTheme.typography.labelLarge)
  }
}

@Composable
private fun CustomSeedColorDialog(
  initialHex: String,
  onApply: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current
  val initialHsl = remember(initialHex) { DynamicColorEngine.hexToHsl(initialHex) }
  var hue by remember { mutableFloatStateOf(initialHsl.h) }
  var sat by remember { mutableFloatStateOf(initialHsl.s.coerceIn(20f, 100f)) }
  var lig by remember { mutableFloatStateOf(initialHsl.l.coerceIn(25f, 80f)) }

  val previewColor = DynamicColorEngine.hslColor(hue, sat, lig)
  val previewHex = DynamicColorEngine.colorToHex(previewColor)

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(tokens.radii.xl),
      color = colors.surfaceContainerHigh,
      border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Custom Seed Color", style = MaterialTheme.typography.titleLarge)
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(previewColor)
              .border(2.dp, colors.onSurface, CircleShape),
          )
        }

        Text("Hue (${hue.roundToInt()}°)", style = MaterialTheme.typography.labelMedium)
        Slider(value = hue, onValueChange = { hue = it }, valueRange = 0f..360f)

        Text("Saturation (${sat.roundToInt()}%)", style = MaterialTheme.typography.labelMedium)
        Slider(value = sat, onValueChange = { sat = it }, valueRange = 0f..100f)

        Text("Lightness (${lig.roundToInt()}%)", style = MaterialTheme.typography.labelMedium)
        Slider(value = lig, onValueChange = { lig = it }, valueRange = 15f..85f)

        Text(
          text = previewHex,
          style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = tokens.monoFamily,
            fontWeight = FontWeight.Bold,
          ),
          color = colors.primary,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        ) {
          OutlinedButton(onClick = onDismiss, shape = CircleShape) {
            Text(stringResource(R.string.cancel))
          }
          Button(onClick = { onApply(previewHex) }, shape = CircleShape) {
            Text("Apply")
          }
        }
      }
    }
  }
}

@Composable
private fun ThemeJsonDialog(
  mode: String,
  initialJson: String,
  onImport: (String) -> Result<Unit>,
  onDismiss: () -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current
  val clipboard = LocalClipboardManager.current
  var text by remember { mutableStateOf(initialJson) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(tokens.radii.xl),
      color = colors.surfaceContainerHigh,
      border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Text(
          text = if (mode == "export") "Theme JSON" else "Import Theme JSON",
          style = MaterialTheme.typography.titleLarge,
        )

        OutlinedTextField(
          value = text,
          onValueChange = {
            if (mode == "import") {
              text = it
              errorMsg = null
            }
          },
          readOnly = mode == "export",
          minLines = 6,
          maxLines = 10,
          textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = tokens.monoFamily),
          shape = RoundedCornerShape(tokens.radii.md),
          modifier = Modifier.fillMaxWidth(),
        )

        if (errorMsg != null) {
          Text(
            text = errorMsg!!,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.error,
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        ) {
          OutlinedButton(onClick = onDismiss, shape = CircleShape) {
            Text(stringResource(R.string.cd_close))
          }
          if (mode == "export") {
            Button(
              onClick = {
                clipboard.setText(AnnotatedString(text))
                onDismiss()
              },
              shape = CircleShape,
            ) {
              Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(6.dp))
              Text("Copy")
            }
          } else {
            Button(
              onClick = {
                val res = onImport(text)
                if (res.isSuccess) {
                  onDismiss()
                } else {
                  errorMsg = "Invalid theme JSON format."
                }
              },
              shape = CircleShape,
            ) {
              Text("Apply")
            }
          }
        }
      }
    }
  }
}
