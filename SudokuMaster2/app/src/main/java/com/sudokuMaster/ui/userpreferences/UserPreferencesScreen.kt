package com.sudokuMaster.ui.userpreferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.SudokuMaster.R
import com.sudokuMaster.data.AppTheme
import com.sudokuMaster.data.DifficultyLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPreferencesScreen(
    navController: NavController,
    userPreferencesViewModel: UserPreferencesViewModel,
    modifier: Modifier = Modifier
) {
    val userPreferences by userPreferencesViewModel.userPreferencesFlow.collectAsState(initial = null) // Usa initial = null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.preferenze_utente), color = MaterialTheme.colorScheme.onPrimaryContainer)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.torna_indietro),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            userPreferences?.let { prefs ->
                Text(text = stringResource(R.string.tema_app), style = MaterialTheme.typography.titleMedium)

                Button(onClick = {
                    val nextTheme = when (prefs.appTheme) {
                        AppTheme.LIGHT -> AppTheme.DARK
                        AppTheme.DARK -> AppTheme.SYSTEM_DEFAULT
                        AppTheme.SYSTEM_DEFAULT -> AppTheme.LIGHT
                        AppTheme.THEME_UNSPECIFIED -> AppTheme.LIGHT
                        else -> AppTheme.LIGHT
                    }
                    userPreferencesViewModel.updateAppTheme(nextTheme)
                }) {
                    Text(stringResource(R.string.tema_attuale, prefs.appTheme.name))
                }
                Spacer(Modifier.height(16.dp))

                Text(text = stringResource(R.string.difficolt_predefinita), style = MaterialTheme.typography.titleMedium)
                Button(onClick = {
                    val nextDifficulty = when (prefs.defaultDifficulty) {
                        DifficultyLevel.EASY -> DifficultyLevel.MEDIUM
                        DifficultyLevel.MEDIUM -> DifficultyLevel.HARD
                        DifficultyLevel.HARD -> DifficultyLevel.EASY
                        DifficultyLevel.DIFFICULTY_UNSPECIFIED -> DifficultyLevel.EASY
                        else -> DifficultyLevel.EASY
                    }
                    userPreferencesViewModel.updateDefaultDifficulty(nextDifficulty)
                }) {
                    Text(stringResource(R.string.difficolt, prefs.defaultDifficulty.name))
                }
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.suono_abilitato), style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = prefs.soundEnabled,
                        onCheckedChange = { userPreferencesViewModel.updateSoundEnabled(it) }
                    )
                }
            } ?: run {
                Text(stringResource(R.string.caricamento_preferenze), style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.weight(1f)) // Spinge il bottone sotto

            Button(onClick = { navController.popBackStack() }) {
                Text(stringResource(R.string.salva_e_torna_indietro))
            }
        }
    }
}