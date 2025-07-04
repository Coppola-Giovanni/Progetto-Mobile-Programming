package com.sudokuMaster.ui.components


import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.isSystemInDarkTheme
import com.sudokuMaster.ui.theme.textColorDark
import com.sudokuMaster.ui.theme.textColorLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolbar(
    modifier: Modifier = Modifier,
    title: String,
    icon: @Composable () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = if (isDarkTheme) textColorDark else textColorLight,
                textAlign = TextAlign.Start,
                maxLines = 1,
            )
        },
        navigationIcon = { icon() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = if (isDarkTheme) textColorDark else textColorLight,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )

    )
}