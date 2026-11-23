package com.example.model

enum class ClockTab(val label: String) {
  CLOCK("Clock"),
  ALARMS("Alarms"),
  TIMER("Timer"),
  STOPWATCH("Stopwatch"),
}

enum class PaletteScheme(val id: String, val label: String) {
  EXPRESSIVE("expressive", "Expressive"),
  VIBRANT("vibrant", "Vibrant"),
  TONAL("tonal", "Tonal"),
  MONO("mono", "Mono");

  companion object {
    fun fromId(id: String): PaletteScheme = entries.firstOrNull { it.id == id } ?: EXPRESSIVE
  }
}

enum class SurfaceMode(val id: String, val label: String) {
  DARK("dark", "Dark"),
  AMOLED("amoled", "AMOLED"),
  LIGHT("light", "Light");

  companion object {
    fun fromId(id: String): SurfaceMode = entries.firstOrNull { it.id == id } ?: DARK
  }
}

enum class DialShape(val id: String, val label: String) {
  COOKIE_9("cookie9", "Cookie 9"),
  CLOVER_4("clover4", "Clover 4"),
  FLOWER("flower", "Flower"),
  SOFT_BURST("softBurst", "Burst"),
  SUNNY("sunny", "Sunny"),
  SQUIRCLE("squircle", "Squircle"),
  CIRCLE("circle", "Circle");

  fun next(): DialShape = entries[(ordinal + 1) % entries.size]

  companion object {
    fun fromId(id: String): DialShape = entries.firstOrNull { it.id == id } ?: COOKIE_9
  }
}

enum class ClockLayout(val id: String, val label: String) {
  HYBRID("hybrid", "Hybrid"),
  ANALOG("analog", "Analog"),
  DIGITAL("digital", "Digital");

  companion object {
    fun fromId(id: String): ClockLayout = entries.firstOrNull { it.id == id } ?: HYBRID
  }
}

enum class HandStyle(val id: String, val label: String) {
  CHUNKY("chunky", "Chunky"),
  NEEDLE("needle", "Needle"),
  ORBIT("orbit", "Orbit");

  companion object {
    fun fromId(id: String): HandStyle = entries.firstOrNull { it.id == id } ?: CHUNKY
  }
}

enum class SweepMode(val id: String, val label: String) {
  SMOOTH("smooth", "Sweep"),
  TICK("tick", "Tick");

  companion object {
    fun fromId(id: String): SweepMode = entries.firstOrNull { it.id == id } ?: SMOOTH
  }
}

enum class FontPersonality(val id: String, val label: String) {
  FLEX("flex", "Flex"),
  OUTFIT("outfit", "Round"),
  SERIF("serif", "Serif"),
  MONO("mono", "Mono");

  companion object {
    fun fromId(id: String): FontPersonality = entries.firstOrNull { it.id == id } ?: FLEX
  }
}

enum class CornerStyle(val id: String, val label: String) {
  EXPRESSIVE("expressive", "Expressive"),
  SOFT("soft", "Soft"),
  SHARP("sharp", "Sharp");

  companion object {
    fun fromId(id: String): CornerStyle = entries.firstOrNull { it.id == id } ?: EXPRESSIVE
  }
}

data class ClockThemeConfig(
  val seedColorHex: String = "#16C768",
  val scheme: PaletteScheme = PaletteScheme.EXPRESSIVE,
  val surface: SurfaceMode = SurfaceMode.DARK,
  val shape: DialShape = DialShape.COOKIE_9,
  val layout: ClockLayout = ClockLayout.HYBRID,
  val handStyle: HandStyle = HandStyle.CHUNKY,
  val sweepMode: SweepMode = SweepMode.SMOOTH,
  val font: FontPersonality = FontPersonality.FLEX,
  val clockWeight: Int = 700,
  val roundness: CornerStyle = CornerStyle.EXPRESSIVE,
  val use24h: Boolean = false,
  val showSeconds: Boolean = true,
  val showNumbers: Boolean = true,
  val alarmTone: String = "marimba",
)

data class CuratedPreset(
  val name: String,
  val seedColorHex: String,
  val scheme: PaletteScheme,
  val surface: SurfaceMode,
  val shape: DialShape,
  val font: FontPersonality,
)

data class WorldCity(
  val name: String,
  val timeZoneId: String,
)

data class AlarmItem(
  val id: Long,
  val hour: Int,
  val minute: Int,
  val label: String,
  val days: List<Int>,
  val enabled: Boolean,
)

data class StopwatchLap(
  val number: Int,
  val splitMs: Long,
  val totalMs: Long,
)

data class RingingAlert(
  val title: String,
  val subtitle: String,
)

object ClockCatalogs {
  val SEED_SWATCHES = listOf(
    "#16C768",
    "#8E75FF",
    "#FF6B57",
    "#2DD4BF",
    "#FFB020",
    "#EC4899",
    "#3B82F6",
    "#84CC16",
  )

  val CURATED_PRESETS = listOf(
    CuratedPreset(
      name = "Doomsday Green",
      seedColorHex = "#16C768",
      scheme = PaletteScheme.EXPRESSIVE,
      surface = SurfaceMode.DARK,
      shape = DialShape.COOKIE_9,
      font = FontPersonality.FLEX,
    ),
    CuratedPreset(
      name = "Expressive Iris",
      seedColorHex = "#8E75FF",
      scheme = PaletteScheme.EXPRESSIVE,
      surface = SurfaceMode.DARK,
      shape = DialShape.FLOWER,
      font = FontPersonality.FLEX,
    ),
    CuratedPreset(
      name = "Pixel Coral",
      seedColorHex = "#FF6B57",
      scheme = PaletteScheme.VIBRANT,
      surface = SurfaceMode.LIGHT,
      shape = DialShape.CLOVER_4,
      font = FontPersonality.OUTFIT,
    ),
    CuratedPreset(
      name = "Solar Citrus",
      seedColorHex = "#FFB020",
      scheme = PaletteScheme.VIBRANT,
      surface = SurfaceMode.AMOLED,
      shape = DialShape.SUNNY,
      font = FontPersonality.OUTFIT,
    ),
    CuratedPreset(
      name = "Editorial Ink",
      seedColorHex = "#E2E2E2",
      scheme = PaletteScheme.MONO,
      surface = SurfaceMode.AMOLED,
      shape = DialShape.SQUIRCLE,
      font = FontPersonality.SERIF,
    ),
    CuratedPreset(
      name = "Oceanic Slate",
      seedColorHex = "#38BDF8",
      scheme = PaletteScheme.TONAL,
      surface = SurfaceMode.LIGHT,
      shape = DialShape.SOFT_BURST,
      font = FontPersonality.MONO,
    ),
  )

  val DEFAULT_CITIES = listOf(
    WorldCity("Tokyo", "Asia/Tokyo"),
    WorldCity("London", "Europe/London"),
    WorldCity("New York", "America/New_York"),
  )

  val CITY_CATALOG = listOf(
    WorldCity("Bengaluru", "Asia/Kolkata"),
    WorldCity("New Delhi", "Asia/Kolkata"),
    WorldCity("Tokyo", "Asia/Tokyo"),
    WorldCity("Seoul", "Asia/Seoul"),
    WorldCity("Singapore", "Asia/Singapore"),
    WorldCity("Dubai", "Asia/Dubai"),
    WorldCity("Sydney", "Australia/Sydney"),
    WorldCity("London", "Europe/London"),
    WorldCity("Paris", "Europe/Paris"),
    WorldCity("Berlin", "Europe/Berlin"),
    WorldCity("Zurich", "Europe/Zurich"),
    WorldCity("New York", "America/New_York"),
    WorldCity("San Francisco", "America/Los_Angeles"),
    WorldCity("Toronto", "America/Toronto"),
    WorldCity("São Paulo", "America/Sao_Paulo"),
    WorldCity("Cairo", "Africa/Cairo"),
  )

  val DEFAULT_ALARMS = listOf(
    AlarmItem(
      id = 1L,
      hour = 7,
      minute = 0,
      label = "Morning Routine",
      days = listOf(1, 2, 3, 4, 5),
      enabled = true,
    ),
    AlarmItem(
      id = 2L,
      hour = 9,
      minute = 30,
      label = "Weekend Brunch",
      days = listOf(0, 6),
      enabled = false,
    ),
  )

  val TIMER_PRESETS_SECONDS = listOf(60, 300, 600, 900, 1500)
}
