package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = MinimalPrimaryDark,
    secondary = MinimalSecondaryDark,
    tertiary = MinimalAccentFAB,
    background = MinimalBackgroundDark,
    surface = MinimalSurfaceDark,
    surfaceVariant = MinimalSurfaceVariantDark,
    onPrimary = MinimalOnPrimaryDark,
    onSecondary = Color.Black,
    onTertiary = MinimalOnPrimaryContainerDark,
    onBackground = MinimalTextDefaultDark,
    onSurface = MinimalTextDefaultDark,
    onSurfaceVariant = MinimalTextSecondaryDark,
    primaryContainer = MinimalPrimaryContainerDark,
    onPrimaryContainer = MinimalOnPrimaryContainerDark,
    secondaryContainer = MinimalSecondaryContainerDark,
    onSecondaryContainer = MinimalOnSecondaryContainerDark,
    outline = MinimalOutlineDark,
    outlineVariant = MinimalOutlineVariantDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MinimalPrimaryLight,
    secondary = MinimalSecondaryLight,
    tertiary = MinimalAccentFAB,
    background = MinimalBackgroundLight,
    surface = MinimalSurfaceLight,
    surfaceVariant = MinimalSurfaceVariantLight,
    onPrimary = MinimalOnPrimaryLight,
    onSecondary = Color.White,
    onTertiary = MinimalOnPrimaryContainerLight,
    onBackground = MinimalTextDefaultLight,
    onSurface = MinimalTextDefaultLight,
    onSurfaceVariant = MinimalTextSecondaryLight,
    primaryContainer = MinimalPrimaryContainerLight,
    onPrimaryContainer = MinimalOnPrimaryContainerLight,
    secondaryContainer = MinimalSecondaryContainerLight,
    onSecondaryContainer = MinimalOnSecondaryContainerLight,
    outline = MinimalOutlineLight,
    outlineVariant = MinimalOutlineVariantLight,
    error = MinimalErrorRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to enforce our dedicated Clean Minimalism theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
