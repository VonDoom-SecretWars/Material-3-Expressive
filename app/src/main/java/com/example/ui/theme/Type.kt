package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.FontPersonality

val RobotoFlexFamily = FontFamily(
  Font(R.font.roboto_flex, FontWeight.Light),
  Font(R.font.roboto_flex, FontWeight.Normal),
  Font(R.font.roboto_flex, FontWeight.Medium),
  Font(R.font.roboto_flex, FontWeight.SemiBold),
  Font(R.font.roboto_flex, FontWeight.Bold),
  Font(R.font.roboto_flex, FontWeight.ExtraBold),
  Font(R.font.roboto_flex, FontWeight.Black),
)

val OutfitFamily = FontFamily(
  Font(R.font.outfit, FontWeight.Light),
  Font(R.font.outfit, FontWeight.Normal),
  Font(R.font.outfit, FontWeight.Medium),
  Font(R.font.outfit, FontWeight.SemiBold),
  Font(R.font.outfit, FontWeight.Bold),
  Font(R.font.outfit, FontWeight.ExtraBold),
  Font(R.font.outfit, FontWeight.Black),
)

val InstrumentSerifFamily = FontFamily(
  Font(R.font.instrument_serif, FontWeight.Normal),
)

val JetBrainsMonoFamily = FontFamily(
  Font(R.font.jetbrains_mono, FontWeight.Light),
  Font(R.font.jetbrains_mono, FontWeight.Normal),
  Font(R.font.jetbrains_mono, FontWeight.Medium),
  Font(R.font.jetbrains_mono, FontWeight.SemiBold),
  Font(R.font.jetbrains_mono, FontWeight.Bold),
  Font(R.font.jetbrains_mono, FontWeight.ExtraBold),
)

fun displayFontFamilyFor(personality: FontPersonality): FontFamily = when (personality) {
  FontPersonality.FLEX -> RobotoFlexFamily
  FontPersonality.OUTFIT -> OutfitFamily
  FontPersonality.SERIF -> InstrumentSerifFamily
  FontPersonality.MONO -> JetBrainsMonoFamily
}

fun clockFontWeightFor(weight: Int): FontWeight {
  return when {
    weight <= 325 -> FontWeight.Light
    weight <= 450 -> FontWeight.Normal
    weight <= 550 -> FontWeight.Medium
    weight <= 650 -> FontWeight.SemiBold
    weight <= 750 -> FontWeight.Bold
    weight <= 850 -> FontWeight.ExtraBold
    else -> FontWeight.Black
  }
}

fun chronosTypography(displayFamily: FontFamily): Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = displayFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 60.sp,
    lineHeight = 64.sp,
    letterSpacing = (-1.5).sp,
  ),
  displayMedium = TextStyle(
    fontFamily = displayFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 44.sp,
    lineHeight = 48.sp,
    letterSpacing = (-1.0).sp,
  ),
  headlineLarge = TextStyle(
    fontFamily = displayFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 30.sp,
    lineHeight = 36.sp,
    letterSpacing = (-0.6).sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = displayFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 30.sp,
    letterSpacing = (-0.3).sp,
  ),
  titleLarge = TextStyle(
    fontFamily = displayFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
  ),
  titleMedium = TextStyle(
    fontFamily = RobotoFlexFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = RobotoFlexFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 22.sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = RobotoFlexFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
  ),
  labelLarge = TextStyle(
    fontFamily = RobotoFlexFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    lineHeight = 18.sp,
  ),
  labelSmall = TextStyle(
    fontFamily = RobotoFlexFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.8.sp,
  ),
)
