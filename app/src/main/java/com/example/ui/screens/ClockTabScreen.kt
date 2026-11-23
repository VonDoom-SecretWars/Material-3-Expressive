package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.ClockCatalogs
import com.example.model.ClockLayout
import com.example.model.ClockThemeConfig
import com.example.model.WorldCity
import com.example.ui.components.AnalogClockDial
import com.example.ui.theme.LocalExpressiveTokens
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ClockTabScreen(
  nowMillis: Long,
  theme: ClockThemeConfig,
  worldCities: List<WorldCity>,
  onTapDialToMorph: () -> Unit,
  onAddCity: (WorldCity) -> Unit,
  onRemoveCity: (WorldCity) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showCityDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 112.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    HeroClockCard(
      nowMillis = nowMillis,
      theme = theme,
      onTapDialToMorph = onTapDialToMorph,
    )

    WorldClockSection(
      nowMillis = nowMillis,
      use24h = theme.use24h,
      cities = worldCities,
      onOpenAddCity = { showCityDialog = true },
      onRemoveCity = onRemoveCity,
    )
  }

  if (showCityDialog) {
    AddCityDialog(
      nowMillis = nowMillis,
      use24h = theme.use24h,
      existingCities = worldCities,
      onSelectCity = { city ->
        onAddCity(city)
        showCityDialog = false
      },
      onDismiss = { showCityDialog = false },
    )
  }
}

@Composable
fun HeroClockCard(
  nowMillis: Long,
  theme: ClockThemeConfig,
  onTapDialToMorph: () -> Unit,
  modifier: Modifier = Modifier,
  isAmbient: Boolean = false,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current

  Surface(
    shape = RoundedCornerShape(tokens.radii.xl),
    color = if (isAmbient) Color.Transparent else colors.surfaceContainerLow,
    border = if (isAmbient) null else BorderStroke(1.dp, colors.outlineVariant),
    modifier = modifier.fillMaxWidth(),
  ) {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .drawBehind {
          if (!isAmbient) {
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(
                  colors.primaryContainer.copy(alpha = 0.35f),
                  Color.Transparent,
                ),
                center = Offset(size.width * 0.85f, size.height * 0.15f),
                radius = size.maxDimension * 0.55f,
              ),
            )
          }
        }
        .padding(horizontal = 24.dp, vertical = if (isAmbient) 32.dp else 28.dp),
    ) {
      val useSideBySide = maxWidth >= 600.dp && theme.layout == ClockLayout.HYBRID

      if (useSideBySide) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(36.dp, Alignment.CenterHorizontally),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          AnalogClockDial(
            nowMillis = nowMillis,
            dialShape = theme.shape,
            handStyle = theme.handStyle,
            sweepMode = theme.sweepMode,
            showSeconds = theme.showSeconds,
            showNumbers = theme.showNumbers,
            onTapDial = onTapDialToMorph,
            dialDescription = stringResource(R.string.cd_morph_dial),
            size = if (isAmbient) 280.dp else 248.dp,
            modifier = Modifier.testTag("hero_analog_dial"),
          )

          DigitalReadoutBlock(
            nowMillis = nowMillis,
            theme = theme,
            centered = false,
            showDigits = true,
            isAmbient = isAmbient,
            modifier = Modifier.weight(1f),
          )
        }
      } else {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
          if (theme.layout != ClockLayout.DIGITAL) {
            AnalogClockDial(
              nowMillis = nowMillis,
              dialShape = theme.shape,
              handStyle = theme.handStyle,
              sweepMode = theme.sweepMode,
              showSeconds = theme.showSeconds,
              showNumbers = theme.showNumbers,
              onTapDial = onTapDialToMorph,
              dialDescription = stringResource(R.string.cd_morph_dial),
              size = if (isAmbient || theme.layout == ClockLayout.ANALOG) 268.dp else 232.dp,
              modifier = Modifier.testTag("hero_analog_dial"),
            )
          }

          DigitalReadoutBlock(
            nowMillis = nowMillis,
            theme = theme,
            centered = true,
            showDigits = theme.layout != ClockLayout.ANALOG,
            isAmbient = isAmbient,
          )
        }
      }
    }
  }
}

@Composable
private fun DigitalReadoutBlock(
  nowMillis: Long,
  theme: ClockThemeConfig,
  centered: Boolean,
  showDigits: Boolean,
  isAmbient: Boolean,
  modifier: Modifier = Modifier,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current

  val cal = remember(nowMillis) {
    Calendar.getInstance().apply { timeInMillis = nowMillis }
  }
  val hrs = cal.get(Calendar.HOUR_OF_DAY)
  val mins = cal.get(Calendar.MINUTE)
  val secs = cal.get(Calendar.SECOND)

  val hourStr = if (theme.use24h) {
    String.format(Locale.US, "%02d", hrs)
  } else {
    val h12 = hrs % 12
    (if (h12 == 0) 12 else h12).toString()
  }
  val minStr = String.format(Locale.US, "%02d", mins)
  val secStr = String.format(Locale.US, "%02d", secs)
  val amPmStr = if (hrs >= 12) "PM" else "AM"

  val dateText = remember(nowMillis / 60_000L) {
    SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date(nowMillis))
  }
  val tzCity = remember {
    TimeZone.getDefault().id.substringAfterLast('/').replace('_', ' ')
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    if (showDigits) {
      Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = "$hourStr:$minStr",
          style = MaterialTheme.typography.displayLarge.copy(
            fontFamily = tokens.displayFamily,
            fontWeight = tokens.clockFontWeight,
            fontSize = if (isAmbient) 68.sp else 58.sp,
            lineHeight = if (isAmbient) 72.sp else 60.sp,
            fontFeatureSettings = "tnum",
          ),
          color = colors.onSurface,
        )

        if (theme.showSeconds) {
          Text(
            text = secStr,
            style = MaterialTheme.typography.headlineMedium.copy(
              fontFamily = tokens.displayFamily,
              fontWeight = FontWeight.SemiBold,
              fontFeatureSettings = "tnum",
            ),
            color = colors.primary,
            modifier = Modifier.padding(bottom = 8.dp),
          )
        }

        if (!theme.use24h) {
          Surface(
            shape = CircleShape,
            color = colors.tertiaryContainer,
            contentColor = colors.onTertiaryContainer,
            modifier = Modifier.padding(bottom = 10.dp),
          ) {
            Text(
              text = amPmStr,
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
          }
        }
      }
    }

    Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp, if (centered) Alignment.CenterHorizontally else Alignment.Start),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = dateText,
        style = MaterialTheme.typography.titleMedium,
        color = colors.onSurfaceVariant,
      )
      Surface(
        shape = CircleShape,
        color = colors.surfaceContainerHigh,
        contentColor = colors.onSurfaceVariant,
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Public,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
          )
          Text(
            text = tzCity,
            style = MaterialTheme.typography.labelMedium,
          )
        }
      }
    }
  }
}

@Composable
private fun WorldClockSection(
  nowMillis: Long,
  use24h: Boolean,
  cities: List<WorldCity>,
  onOpenAddCity: () -> Unit,
  onRemoveCity: (WorldCity) -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(R.string.world_clock_title),
        style = MaterialTheme.typography.titleLarge,
        color = colors.onSurface,
      )
      Button(
        onClick = onOpenAddCity,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = colors.secondaryContainer,
          contentColor = colors.onSecondaryContainer,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = Modifier.testTag("add_city_button"),
      ) {
        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.add_city))
      }
    }

    if (cities.isEmpty()) {
      Surface(
        shape = RoundedCornerShape(tokens.radii.lg),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Public,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(32.dp),
          )
          Text(
            text = stringResource(R.string.empty_world_cities),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
          )
        }
      }
    } else {
      BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 600.dp) 2 else 1
        val rows = cities.chunked(columns)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          rows.forEach { rowCities ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              rowCities.forEach { city ->
                WorldCityCard(
                  city = city,
                  nowMillis = nowMillis,
                  use24h = use24h,
                  onRemove = { onRemoveCity(city) },
                  modifier = Modifier.weight(1f),
                )
              }
              if (rowCities.size < columns) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun WorldCityCard(
  city: WorldCity,
  nowMillis: Long,
  use24h: Boolean,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current

  val cityTz = remember(city.timeZoneId) { TimeZone.getTimeZone(city.timeZoneId) }
  val timeFormatted = remember(nowMillis / 1000L, use24h, city.timeZoneId) {
    val pattern = if (use24h) "HH:mm" else "h:mm a"
    SimpleDateFormat(pattern, Locale.getDefault()).apply { timeZone = cityTz }.format(Date(nowMillis))
  }
  val cityCal = remember(nowMillis / 60_000L, city.timeZoneId) {
    Calendar.getInstance(cityTz).apply { timeInMillis = nowMillis }
  }
  val localCal = remember(nowMillis / 60_000L) {
    Calendar.getInstance().apply { timeInMillis = nowMillis }
  }
  val cityHour = cityCal.get(Calendar.HOUR_OF_DAY)
  val isDaytime = cityHour in 6..17

  val dayDiffText = remember(
    cityCal.get(Calendar.YEAR),
    cityCal.get(Calendar.DAY_OF_YEAR),
    localCal.get(Calendar.YEAR),
    localCal.get(Calendar.DAY_OF_YEAR),
  ) {
    val cityDays = cityCal.get(Calendar.YEAR) * 400 + cityCal.get(Calendar.DAY_OF_YEAR)
    val localDays = localCal.get(Calendar.YEAR) * 400 + localCal.get(Calendar.DAY_OF_YEAR)
    when {
      cityDays == localDays -> "Today"
      cityDays > localDays -> "Tomorrow"
      else -> "Yesterday"
    }
  }

  Surface(
    shape = RoundedCornerShape(tokens.radii.lg),
    color = colors.surfaceContainer,
    border = BorderStroke(1.dp, colors.outlineVariant),
    modifier = modifier,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = city.name,
          style = MaterialTheme.typography.titleMedium,
          color = colors.onSurface,
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = if (isDaytime) Icons.Rounded.WbSunny else Icons.Rounded.Bedtime,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = dayDiffText,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = timeFormatted,
          style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = tokens.displayFamily,
            fontFeatureSettings = "tnum",
          ),
          color = colors.onSurface,
        )
        IconButton(onClick = onRemove) {
          Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = stringResource(R.string.cd_remove_city),
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun AddCityDialog(
  nowMillis: Long,
  use24h: Boolean,
  existingCities: List<WorldCity>,
  onSelectCity: (WorldCity) -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  val tokens = LocalExpressiveTokens.current
  var query by remember { mutableStateOf("") }

  val filtered = remember(query, existingCities) {
    val q = query.trim().lowercase()
    ClockCatalogs.CITY_CATALOG.filter { city ->
      (q.isEmpty() || city.name.lowercase().contains(q) || city.timeZoneId.lowercase().contains(q))
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(tokens.radii.xl),
      color = colors.surfaceContainerHigh,
      border = BorderStroke(1.dp, colors.outlineVariant),
      modifier = Modifier.fillMaxWidth(),
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
          Text(
            text = stringResource(R.string.add_city),
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cd_close))
          }
        }

        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text(stringResource(R.string.search_city_hint)) },
          leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
          singleLine = true,
          shape = RoundedCornerShape(tokens.radii.md),
          modifier = Modifier.fillMaxWidth(),
        )

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(filtered, key = { "${it.name}_${it.timeZoneId}" }) { city ->
            val timeStr = remember(nowMillis / 60_000L, use24h, city.timeZoneId) {
              val pattern = if (use24h) "HH:mm" else "h:mm a"
              SimpleDateFormat(pattern, Locale.getDefault())
                .apply { timeZone = TimeZone.getTimeZone(city.timeZoneId) }
                .format(Date(nowMillis))
            }
            val alreadyAdded = existingCities.any { it.name == city.name }
            Surface(
              onClick = { onSelectCity(city) },
              enabled = !alreadyAdded,
              shape = RoundedCornerShape(tokens.radii.md),
              color = if (alreadyAdded) colors.surfaceContainerLowest else colors.surfaceContainer,
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column {
                  Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (alreadyAdded) colors.onSurfaceVariant else colors.onSurface,
                  )
                  Text(
                    text = city.timeZoneId,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                  )
                }
                Text(
                  text = if (alreadyAdded) "Added" else timeStr,
                  style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                  color = if (alreadyAdded) colors.onSurfaceVariant else colors.primary,
                )
              }
            }
          }
        }
      }
    }
  }
}
