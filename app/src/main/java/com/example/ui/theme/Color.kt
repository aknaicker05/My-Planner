package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Clean Minimalism Theme Colors (Extracted from Design HTML)
val MinimalBackgroundLight = Color(0xFFFCF8F9)        // `#FCF8F9` - Soft warm off-white canvas
val MinimalTextDefaultLight = Color(0xFF1D1B20)       // `#1D1B20` - Deep off-black charcoal text
val MinimalTextSecondaryLight = Color(0xFF49454F)     // `#49454F` - Muted slate gray text

val MinimalPrimaryLight = Color(0xFF6750A4)           // `#6750A4` - Royal M3 purple
val MinimalOnPrimaryLight = Color(0xFFFFFFFF)
val MinimalPrimaryContainerLight = Color(0xFFEADDFF)  // `#EADDFF` - Soft lavender badge/accent bg
val MinimalOnPrimaryContainerLight = Color(0xFF21005D) // `#21005D` - Deep amethyst label text

val MinimalSecondaryLight = Color(0xFF625B71)         // Stable M3 secondary purple
val MinimalSecondaryContainerLight = Color(0xFFE8DEF8) // `#E8DEF8` - Bright accent pill/icon background
val MinimalOnSecondaryContainerLight = Color(0xFF21005D)

val MinimalSurfaceLight = Color(0xFFFFFFFF)           // `#FFFFFF` - Crisp white surfaces
val MinimalSurfaceVariantLight = Color(0xFFF3EDF7)   // `#F3EDF7` - Elegant light-lavender raised background
val MinimalOnSurfaceLight = Color(0xFF1D1B20)

val MinimalOutlineLight = Color(0xFFCAC4D0)          // `#CAC4D0` - Primary content border
val MinimalOutlineVariantLight = Color(0xFFE7E0EC)   // `#E7E0EC` - Secondary divider or subtle boundary

val MinimalAccentFAB = Color(0xFFD0BCFF)              // `#D0BCFF` - Iconic lilac floating action color
val MinimalErrorRed = Color(0xFFB3261E)               // `#B3261E` - Urgent task red alert

// Refined Dark Mode Companions (Deep slate-purple for stunning, eye-strain-free visuals)
val MinimalBackgroundDark = Color(0xFF141218)
val MinimalTextDefaultDark = Color(0xFFE6E1E5)
val MinimalTextSecondaryDark = Color(0xFF938F99)

val MinimalPrimaryDark = Color(0xFFD0BCFF)
val MinimalOnPrimaryDark = Color(0xFF381E72)
val MinimalPrimaryContainerDark = Color(0xFF4F378B)
val MinimalOnPrimaryContainerDark = Color(0xFFEADDFF)

val MinimalSecondaryDark = Color(0xFFCCC2DC)
val MinimalSecondaryContainerDark = Color(0xFF4A4458)
val MinimalOnSecondaryContainerDark = Color(0xFFE8DEF8)

val MinimalSurfaceDark = Color(0xFF1D1B20)
val MinimalSurfaceVariantDark = Color(0xFF49454F)
val MinimalOnSurfaceDark = Color(0xFFE6E1E5)

val MinimalOutlineDark = Color(0xFF938F99)
val MinimalOutlineVariantDark = Color(0xFF49454F)

// Legacy bindings for backward compatibility in codebase
val GlobalTealLight = MinimalPrimaryLight
val GlobalIndigoLight = MinimalSecondaryLight
val GlobalAmberLight = MinimalAccentFAB
val GlobalBackgroundLight = MinimalBackgroundLight
val GlobalSurfaceLight = MinimalSurfaceLight

val GlobalTealDark = MinimalPrimaryDark
val GlobalIndigoDark = MinimalSecondaryDark
val GlobalAmberDark = MinimalAccentFAB
val GlobalBackgroundDark = MinimalBackgroundDark
val GlobalSurfaceDark = MinimalSurfaceDark

val Purple80 = MinimalPrimaryDark
val PurpleGrey80 = MinimalSecondaryDark
val Pink80 = MinimalAccentFAB

val Purple40 = MinimalPrimaryLight
val PurpleGrey40 = MinimalSecondaryLight
val Pink40 = MinimalAccentFAB
