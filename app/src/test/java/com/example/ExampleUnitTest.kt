package com.example

import com.example.model.ClockThemeConfig
import com.example.model.DialShape
import com.example.model.PaletteScheme
import com.example.model.SurfaceMode
import com.example.ui.components.computeMaterialShapePoints
import com.example.ui.theme.DynamicColorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun dynamicColorEngine_generatesDistinctDarkAndLightSurfaces() {
    val dark = DynamicColorEngine.generateColorScheme(
      ClockThemeConfig(seedColorHex = "#8E75FF", scheme = PaletteScheme.EXPRESSIVE, surface = SurfaceMode.DARK),
    )
    val amoled = DynamicColorEngine.generateColorScheme(
      ClockThemeConfig(seedColorHex = "#8E75FF", scheme = PaletteScheme.EXPRESSIVE, surface = SurfaceMode.AMOLED),
    )
    val light = DynamicColorEngine.generateColorScheme(
      ClockThemeConfig(seedColorHex = "#8E75FF", scheme = PaletteScheme.EXPRESSIVE, surface = SurfaceMode.LIGHT),
    )

    assertEquals("#16C768", ClockThemeConfig().seedColorHex)
    assertNotEquals(dark.surface, light.surface)
    assertEquals(0xFF000000.toInt(), amoled.surface.hashCode().let { android.graphics.Color.BLACK }.let {
      if (amoled.surface.red == 0f && amoled.surface.green == 0f && amoled.surface.blue == 0f) android.graphics.Color.BLACK else 0
    })
  }

  @Test
  fun materialShapePoints_alwaysGenerateExpectedSampleCount() {
    DialShape.entries.forEach { shape ->
      val pts = computeMaterialShapePoints(shape = shape, cx = 100f, cy = 100f, radius = 88f)
      assertEquals(180, pts.size)
    }
  }
}
