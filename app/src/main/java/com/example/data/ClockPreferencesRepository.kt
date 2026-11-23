package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AlarmItem
import com.example.model.ClockCatalogs
import com.example.model.ClockLayout
import com.example.model.ClockThemeConfig
import com.example.model.CornerStyle
import com.example.model.DialShape
import com.example.model.FontPersonality
import com.example.model.HandStyle
import com.example.model.PaletteScheme
import com.example.model.SurfaceMode
import com.example.model.SweepMode
import com.example.model.WorldCity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.chronosDataStore by preferencesDataStore(name = "chronos_preferences")

class ClockPreferencesRepository(private val context: Context) {

  private object Keys {
    val THEME_JSON = stringPreferencesKey("m3e_clock_theme")
    val CITIES_JSON = stringPreferencesKey("m3e_world_cities")
    val ALARMS_JSON = stringPreferencesKey("m3e_alarms")
  }

  val themeFlow: Flow<ClockThemeConfig> = context.chronosDataStore.data.map { prefs ->
    val raw = prefs[Keys.THEME_JSON]
    if (raw.isNullOrBlank()) ClockThemeConfig()
    else parseThemeJson(raw).getOrDefault(ClockThemeConfig())
  }

  val worldCitiesFlow: Flow<List<WorldCity>> = context.chronosDataStore.data.map { prefs ->
    val raw = prefs[Keys.CITIES_JSON]
    if (raw.isNullOrBlank()) ClockCatalogs.DEFAULT_CITIES
    else parseCitiesJson(raw)
  }

  val alarmsFlow: Flow<List<AlarmItem>> = context.chronosDataStore.data.map { prefs ->
    val raw = prefs[Keys.ALARMS_JSON]
    if (raw.isNullOrBlank()) ClockCatalogs.DEFAULT_ALARMS
    else parseAlarmsJson(raw)
  }

  suspend fun saveTheme(config: ClockThemeConfig) {
    context.chronosDataStore.edit { prefs ->
      prefs[Keys.THEME_JSON] = encodeThemeJson(config)
    }
  }

  suspend fun saveWorldCities(cities: List<WorldCity>) {
    context.chronosDataStore.edit { prefs ->
      val arr = JSONArray()
      cities.forEach { city ->
        arr.put(
          JSONObject().apply {
            put("name", city.name)
            put("tz", city.timeZoneId)
          },
        )
      }
      prefs[Keys.CITIES_JSON] = arr.toString()
    }
  }

  suspend fun saveAlarms(alarms: List<AlarmItem>) {
    context.chronosDataStore.edit { prefs ->
      val arr = JSONArray()
      alarms.forEach { alarm ->
        val daysArr = JSONArray()
        alarm.days.forEach { daysArr.put(it) }
        arr.put(
          JSONObject().apply {
            put("id", alarm.id)
            put("hour", alarm.hour)
            put("minute", alarm.minute)
            put("label", alarm.label)
            put("days", daysArr)
            put("enabled", alarm.enabled)
          },
        )
      }
      prefs[Keys.ALARMS_JSON] = arr.toString()
    }
  }

  fun encodeThemeJson(config: ClockThemeConfig): String {
    return JSONObject()
      .apply {
        put("seedColor", config.seedColorHex)
        put("scheme", config.scheme.id)
        put("surface", config.surface.id)
        put("shape", config.shape.id)
        put("layout", config.layout.id)
        put("handStyle", config.handStyle.id)
        put("sweepMode", config.sweepMode.id)
        put("font", config.font.id)
        put("weight", config.clockWeight)
        put("roundness", config.roundness.id)
        put("use24h", config.use24h)
        put("showSeconds", config.showSeconds)
        put("showNumbers", config.showNumbers)
        put("alarmTone", config.alarmTone)
      }
      .toString(2)
  }

  fun parseThemeJson(raw: String): Result<ClockThemeConfig> = runCatching {
    val obj = JSONObject(raw)
    val defaults = ClockThemeConfig()
    val seedHex = obj.optString("seedColor", defaults.seedColorHex).let { hex ->
      if (Regex("^#[0-9A-Fa-f]{6}$").matches(hex)) hex.uppercase() else defaults.seedColorHex
    }
    ClockThemeConfig(
      seedColorHex = seedHex,
      scheme = PaletteScheme.fromId(obj.optString("scheme", defaults.scheme.id)),
      surface = SurfaceMode.fromId(obj.optString("surface", defaults.surface.id)),
      shape = DialShape.fromId(obj.optString("shape", defaults.shape.id)),
      layout = ClockLayout.fromId(obj.optString("layout", defaults.layout.id)),
      handStyle = HandStyle.fromId(obj.optString("handStyle", defaults.handStyle.id)),
      sweepMode = SweepMode.fromId(obj.optString("sweepMode", defaults.sweepMode.id)),
      font = FontPersonality.fromId(obj.optString("font", defaults.font.id)),
      clockWeight = obj.optInt("weight", defaults.clockWeight).coerceIn(300, 900),
      roundness = CornerStyle.fromId(obj.optString("roundness", defaults.roundness.id)),
      use24h = obj.optBoolean("use24h", defaults.use24h),
      showSeconds = obj.optBoolean("showSeconds", defaults.showSeconds),
      showNumbers = obj.optBoolean("showNumbers", defaults.showNumbers),
      alarmTone = obj.optString("alarmTone", defaults.alarmTone),
    )
  }

  private fun parseCitiesJson(raw: String): List<WorldCity> =
    runCatching {
      val arr = JSONArray(raw)
      buildList {
        for (i in 0 until arr.length()) {
          val o = arr.getJSONObject(i)
          add(WorldCity(name = o.getString("name"), timeZoneId = o.getString("tz")))
        }
      }
    }.getOrDefault(ClockCatalogs.DEFAULT_CITIES)

  private fun parseAlarmsJson(raw: String): List<AlarmItem> =
    runCatching {
      val arr = JSONArray(raw)
      buildList {
        for (i in 0 until arr.length()) {
          val o = arr.getJSONObject(i)
          val daysJson = o.optJSONArray("days") ?: JSONArray()
          val days = buildList {
            for (d in 0 until daysJson.length()) {
              add(daysJson.getInt(d))
            }
          }
          add(
            AlarmItem(
              id = o.getLong("id"),
              hour = o.getInt("hour").coerceIn(0, 23),
              minute = o.getInt("minute").coerceIn(0, 59),
              label = o.optString("label", "Alarm"),
              days = days,
              enabled = o.optBoolean("enabled", true),
            ),
          )
        }
      }
    }.getOrDefault(ClockCatalogs.DEFAULT_ALARMS)
}
