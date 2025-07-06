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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sudokuMaster.data.AppTheme
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.ui.Screen

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
                    Text("Preferenze Utente", color = MaterialTheme.colorScheme.onPrimaryContainer)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Torna indietro",
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
                // Esempio: Selezione del Tema
                Text(text = "Tema App:", style = MaterialTheme.typography.titleMedium)
                // Puoi usare un DropdownMenu, RadioButtons o Slider per selezionare il tema
                // Per semplicità, usiamo un bottone per cambiare ciclicamente il tema
                Button(onClick = {
                    val nextTheme = when (prefs.appTheme) {
                        AppTheme.LIGHT -> AppTheme.DARK
                        AppTheme.DARK -> AppTheme.SYSTEM_DEFAULT
                        AppTheme.SYSTEM_DEFAULT -> AppTheme.LIGHT
                        AppTheme.THEME_UNSPECIFIED -> AppTheme.LIGHT // Default se non specificato
                        else -> AppTheme.LIGHT
                    }
                    userPreferencesViewModel.updateAppTheme(nextTheme)
                }) {
                    Text("Tema attuale: ${prefs.appTheme.name}")
                }
                Spacer(Modifier.height(16.dp))

                // Esempio: Selezione Difficoltà Default
                Text(text = "Difficoltà predefinita:", style = MaterialTheme.typography.titleMedium)
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
                    Text("Difficoltà: ${prefs.defaultDifficulty.name}")
                }
                Spacer(Modifier.height(16.dp))

                // Esempio: Abilitare/Disabilitare Suono
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Suono abilitato:", style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = prefs.soundEnabled,
                        onCheckedChange = { userPreferencesViewModel.updateSoundEnabled(it) }
                    )
                }
            } ?: run {
                // Caricamento o errore
                Text("Caricamento preferenze...", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.weight(1f)) // Spinge il bottone sotto

            Button(onClick = { navController.popBackStack() }) {
                Text("Salva e Torna Indietro (cambiamenti salvati automaticamente)")
            }
        }
    }
}
