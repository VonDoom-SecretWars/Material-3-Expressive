package com.example.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.ChimeSynthesizer
import com.example.data.ClockPreferencesRepository
import com.example.haptics.HapticController
import com.example.model.AlarmItem
import com.example.model.ClockLayout
import com.example.model.ClockTab
import com.example.model.ClockThemeConfig
import com.example.model.CornerStyle
import com.example.model.CuratedPreset
import com.example.model.DialShape
import com.example.model.FontPersonality
import com.example.model.HandStyle
import com.example.model.PaletteScheme
import com.example.model.RingingAlert
import com.example.model.StopwatchLap
import com.example.model.SurfaceMode
import com.example.model.SweepMode
import com.example.model.WorldCity
import java.util.Calendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ChronosUiState(
  val selectedTab: ClockTab = ClockTab.CLOCK,
  val theme: ClockThemeConfig = ClockThemeConfig(),
  val worldCities: List<WorldCity> = emptyList(),
  val alarms: List<AlarmItem> = emptyList(),
  val nowMillis: Long = System.currentTimeMillis(),
  val timerTotalMs: Long = 300_000L,
  val timerRemainMs: Long = 300_000L,
  val timerRunning: Boolean = false,
  val timerWavePhase: Float = 0f,
  val stopwatchElapsedMs: Long = 0L,
  val stopwatchRunning: Boolean = false,
  val stopwatchLaps: List<StopwatchLap> = emptyList(),
  val isAmbientMode: Boolean = false,
  val isThemeSheetOpen: Boolean = false,
  val ringingAlert: RingingAlert? = null,
)

class ChronosViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = ClockPreferencesRepository(application.applicationContext)
  private val chimeSynthesizer = ChimeSynthesizer()
  private val hapticController = HapticController(application.applicationContext)

  private val _uiState = MutableStateFlow(ChronosUiState())
  val uiState: StateFlow<ChronosUiState> = _uiState.asStateFlow()

  private var lastCheckedMinuteKey: String = ""
  private var timerLastElapsedRealtime = 0L
  private var swLastElapsedRealtime = 0L

  init {
    viewModelScope.launch {
      repository.themeFlow.collect { loadedTheme ->
        _uiState.update { it.copy(theme = loadedTheme) }
      }
    }
    viewModelScope.launch {
      repository.worldCitiesFlow.collect { loadedCities ->
        _uiState.update { it.copy(worldCities = loadedCities) }
      }
    }
    viewModelScope.launch {
      repository.alarmsFlow.collect { loadedAlarms ->
        _uiState.update { it.copy(alarms = loadedAlarms) }
      }
    }
    startMainTicker()
  }

  private fun startMainTicker() {
    viewModelScope.launch {
      while (isActive) {
        val state = _uiState.value
        val needsHighFps = state.timerRunning ||
          state.stopwatchRunning ||
          ((state.selectedTab == ClockTab.CLOCK || state.isAmbientMode) &&
            state.theme.sweepMode == SweepMode.SMOOTH)

        val currentSysMs = System.currentTimeMillis()
        val elapsedNow = SystemClock.elapsedRealtime()

        // Check alarms on minute change
        val cal = Calendar.getInstance().apply { timeInMillis = currentSysMs }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday .. 6 = Saturday
        val hr = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        val minuteKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}-$hr-$min"
        if (minuteKey != lastCheckedMinuteKey) {
          val isInitialCheck = lastCheckedMinuteKey.isEmpty()
          lastCheckedMinuteKey = minuteKey
          if (!isInitialCheck) {
            checkAlarms(state.alarms, dayOfWeek, hr, min, state.theme.alarmTone)
          }
        }

        // Update Timer
        var nextTimerRemain = state.timerRemainMs
        var nextTimerRunning = state.timerRunning
        var nextWavePhase = state.timerWavePhase
        var timerFinishedNow = false
        if (state.timerRunning) {
          val dt = (elapsedNow - timerLastElapsedRealtime).coerceAtLeast(0L)
          timerLastElapsedRealtime = elapsedNow
          nextTimerRemain = (state.timerRemainMs - dt).coerceAtLeast(0L)
          nextWavePhase = (state.timerWavePhase + dt * 0.006f) % (Math.PI.toFloat() * 200f)
          if (nextTimerRemain == 0L) {
            nextTimerRunning = false
            timerFinishedNow = true
          }
        }

        // Update Stopwatch
        var nextSwElapsed = state.stopwatchElapsedMs
        if (state.stopwatchRunning) {
          val dt = (elapsedNow - swLastElapsedRealtime).coerceAtLeast(0L)
          swLastElapsedRealtime = elapsedNow
          nextSwElapsed += dt
        }

        _uiState.update { cur ->
          cur.copy(
            nowMillis = currentSysMs,
            timerRemainMs = nextTimerRemain,
            timerRunning = nextTimerRunning,
            timerWavePhase = nextWavePhase,
            stopwatchElapsedMs = nextSwElapsed,
          )
        }

        if (timerFinishedNow) {
          triggerRinging(
            title = "Timer Finished",
            subtitle = "Countdown complete",
            tone = state.theme.alarmTone,
          )
        }

        delay(if (needsHighFps) 16L else 200L)
      }
    }
  }

  private fun checkAlarms(
    alarms: List<AlarmItem>,
    dayOfWeek: Int,
    hour: Int,
    minute: Int,
    tone: String,
  ) {
    val matching = alarms.firstOrNull { alarm ->
      alarm.enabled &&
        alarm.hour == hour &&
        alarm.minute == minute &&
        (alarm.days.isEmpty() || dayOfWeek in alarm.days)
    }
    if (matching != null) {
      val timeLabel = String.format("%02d:%02d", hour, minute)
      triggerRinging(
        title = matching.label.ifBlank { "Alarm" },
        subtitle = timeLabel,
        tone = tone,
      )
    }
  }

  private fun triggerRinging(title: String, subtitle: String, tone: String) {
    _uiState.update { it.copy(ringingAlert = RingingAlert(title, subtitle)) }
    chimeSynthesizer.playChime(tone = tone, singleBurst = false)
    hapticController.startAlarmPulse()
  }

  fun playTestSound() {
    hapticController.doubleTick()
    chimeSynthesizer.playChime(tone = _uiState.value.theme.alarmTone, singleBurst = true)
  }

  fun dismissRinging() {
    chimeSynthesizer.stop()
    hapticController.stopAlarmPulse()
    hapticController.click()
    _uiState.update { it.copy(ringingAlert = null) }
  }

  // Navigation & Overlays
  fun selectTab(tab: ClockTab) {
    hapticController.tick()
    _uiState.update { it.copy(selectedTab = tab) }
  }

  fun setThemeSheetOpen(open: Boolean) {
    hapticController.click()
    _uiState.update { it.copy(isThemeSheetOpen = open) }
  }

  fun toggleAmbientMode() {
    hapticController.click()
    _uiState.update { it.copy(isAmbientMode = !it.isAmbientMode) }
  }

  fun setAmbientMode(enabled: Boolean) {
    hapticController.click()
    _uiState.update { it.copy(isAmbientMode = enabled) }
  }

  // Theme Studio Actions
  private fun updateTheme(transform: (ClockThemeConfig) -> ClockThemeConfig) {
    val updated = transform(_uiState.value.theme)
    _uiState.update { it.copy(theme = updated) }
    viewModelScope.launch { repository.saveTheme(updated) }
  }

  fun cycleSurfaceMode() {
    hapticController.tick()
    updateTheme { cur ->
      val order = SurfaceMode.entries
      cur.copy(surface = order[(cur.surface.ordinal + 1) % order.size])
    }
  }

  fun cycleNextShape() {
    hapticController.heavyClick()
    updateTheme { it.copy(shape = it.shape.next()) }
  }

  fun setSeedColor(hex: String) = updateTheme { it.copy(seedColorHex = hex.uppercase()) }
  fun setPaletteScheme(scheme: PaletteScheme) = updateTheme { it.copy(scheme = scheme) }
  fun setSurfaceMode(mode: SurfaceMode) = updateTheme { it.copy(surface = mode) }
  fun setDialShape(shape: DialShape) = updateTheme { it.copy(shape = shape) }
  fun setClockLayout(layout: ClockLayout) = updateTheme { it.copy(layout = layout) }
  fun setHandStyle(style: HandStyle) = updateTheme { it.copy(handStyle = style) }
  fun setSweepMode(mode: SweepMode) = updateTheme { it.copy(sweepMode = mode) }
  fun setFontPersonality(font: FontPersonality) = updateTheme { it.copy(font = font) }
  fun setClockWeight(weight: Int) = updateTheme { it.copy(clockWeight = weight.coerceIn(300, 900)) }
  fun setCornerStyle(style: CornerStyle) = updateTheme { it.copy(roundness = style) }
  fun toggleUse24h() = updateTheme { it.copy(use24h = !it.use24h) }
  fun toggleShowSeconds() = updateTheme { it.copy(showSeconds = !it.showSeconds) }
  fun toggleShowNumbers() = updateTheme { it.copy(showNumbers = !it.showNumbers) }

  fun applyPreset(preset: CuratedPreset) {
    updateTheme { cur ->
      cur.copy(
        seedColorHex = preset.seedColorHex,
        scheme = preset.scheme,
        surface = preset.surface,
        shape = preset.shape,
        font = preset.font,
      )
    }
  }

  fun resetTheme() {
    updateTheme { ClockThemeConfig() }
  }

  fun exportThemeJson(): String = repository.encodeThemeJson(_uiState.value.theme)

  fun importThemeJson(raw: String): Result<Unit> {
    val parsed = repository.parseThemeJson(raw)
    return parsed.map { config ->
      _uiState.update { it.copy(theme = config) }
      viewModelScope.launch { repository.saveTheme(config) }
    }
  }

  // World Clock Actions
  fun addWorldCity(city: WorldCity) {
    val current = _uiState.value.worldCities
    if (current.any { it.name == city.name && it.timeZoneId == city.timeZoneId }) return
    val updated = current + city
    _uiState.update { it.copy(worldCities = updated) }
    viewModelScope.launch { repository.saveWorldCities(updated) }
  }

  fun removeWorldCity(city: WorldCity) {
    val updated = _uiState.value.worldCities.filterNot {
      it.name == city.name && it.timeZoneId == city.timeZoneId
    }
    _uiState.update { it.copy(worldCities = updated) }
    viewModelScope.launch { repository.saveWorldCities(updated) }
  }

  // Alarm Actions
  fun addAlarm(hour: Int, minute: Int, label: String, days: List<Int>) {
    val newAlarm = AlarmItem(
      id = System.currentTimeMillis(),
      hour = hour.coerceIn(0, 23),
      minute = minute.coerceIn(0, 59),
      label = label.trim().ifEmpty { "Alarm" },
      days = days.sorted(),
      enabled = true,
    )
    val updated = _uiState.value.alarms + newAlarm
    _uiState.update { it.copy(alarms = updated) }
    viewModelScope.launch { repository.saveAlarms(updated) }
  }

  fun toggleAlarm(id: Long) {
    hapticController.tick()
    val updated = _uiState.value.alarms.map { alarm ->
      if (alarm.id == id) alarm.copy(enabled = !alarm.enabled) else alarm
    }
    _uiState.update { it.copy(alarms = updated) }
    viewModelScope.launch { repository.saveAlarms(updated) }
  }

  fun deleteAlarm(id: Long) {
    hapticController.click()
    val updated = _uiState.value.alarms.filterNot { it.id == id }
    _uiState.update { it.copy(alarms = updated) }
    viewModelScope.launch { repository.saveAlarms(updated) }
  }

  // Timer Actions
  fun setTimerPreset(seconds: Int) {
    hapticController.tick()
    val totalMs = seconds * 1000L
    _uiState.update {
      it.copy(
        timerRunning = false,
        timerTotalMs = totalMs,
        timerRemainMs = totalMs,
      )
    }
  }

  fun toggleTimer() {
    hapticController.heavyClick()
    val cur = _uiState.value
    val startingRemain = if (cur.timerRemainMs <= 0L) cur.timerTotalMs else cur.timerRemainMs
    timerLastElapsedRealtime = SystemClock.elapsedRealtime()
    _uiState.update {
      it.copy(
        timerRemainMs = startingRemain,
        timerRunning = !cur.timerRunning,
      )
    }
  }

  fun resetTimer() {
    hapticController.click()
    _uiState.update {
      it.copy(
        timerRunning = false,
        timerRemainMs = it.timerTotalMs,
      )
    }
  }

  fun addTimerMinute() {
    hapticController.tick()
    _uiState.update {
      it.copy(
        timerTotalMs = it.timerTotalMs + 60_000L,
        timerRemainMs = it.timerRemainMs + 60_000L,
      )
    }
  }

  // Stopwatch Actions
  fun toggleStopwatch() {
    hapticController.heavyClick()
    val cur = _uiState.value
    swLastElapsedRealtime = SystemClock.elapsedRealtime()
    _uiState.update { it.copy(stopwatchRunning = !cur.stopwatchRunning) }
  }

  fun resetStopwatch() {
    hapticController.click()
    _uiState.update {
      it.copy(
        stopwatchRunning = false,
        stopwatchElapsedMs = 0L,
        stopwatchLaps = emptyList(),
      )
    }
  }

  fun recordLap() {
    val cur = _uiState.value
    if (!cur.stopwatchRunning && cur.stopwatchElapsedMs == 0L) return
    hapticController.doubleTick()
    val prevTotal = cur.stopwatchLaps.firstOrNull()?.totalMs ?: 0L
    val newLap = StopwatchLap(
      number = cur.stopwatchLaps.size + 1,
      splitMs = (cur.stopwatchElapsedMs - prevTotal).coerceAtLeast(0L),
      totalMs = cur.stopwatchElapsedMs,
    )
    _uiState.update { it.copy(stopwatchLaps = listOf(newLap) + it.stopwatchLaps) }
  }

  override fun onCleared() {
    super.onCleared()
    chimeSynthesizer.stop()
    hapticController.stopAlarmPulse()
  }
}
