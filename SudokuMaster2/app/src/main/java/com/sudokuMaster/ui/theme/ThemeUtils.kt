package com.sudokuMaster.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.luminance

fun ColorScheme.isDark(): Boolean {
    return background.luminance() < 0.5f
}