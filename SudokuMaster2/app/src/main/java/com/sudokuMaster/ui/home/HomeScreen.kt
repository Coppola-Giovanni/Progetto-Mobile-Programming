package com.sudokuMaster.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.SudokuMaster.R
import com.sudokuMaster.ui.theme.isDark
import com.sudokuMaster.ui.userpreferences.UserPreferencesViewModel
import com.sudokuMaster.ui.userpreferences.SoundAndMusicPlayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    userPreferencesViewModel: UserPreferencesViewModel,
    onNewGameClick: () -> Unit,
    onContinueGameClick: () -> Unit,
    onViewStatisticsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentThemeDark = MaterialTheme.colorScheme.isDark()
    val prefs by userPreferencesViewModel.userPreferencesFlow.collectAsState(initial = null)
    val soundAndMusicPlayer = SoundAndMusicPlayer(LocalContext.current)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Sudoku Master", color = MaterialTheme.colorScheme.onPrimaryContainer)
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("user_preferences_screen")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.impostazioni_utente),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            Image(
                painter = painterResource(
                    id = if (isCurrentThemeDark) R.drawable.dark_mode_background else R.drawable.light_mode_background
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sudoku Master",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Cursive,
                        color = if (isCurrentThemeDark) Color.White else Color.Black
                    ),
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                Button(
                    onClick = {
                        if (prefs?.soundEnabled == true) soundAndMusicPlayer.playSoundEffect(R.raw.button_click_sound)
                        onNewGameClick()
                    },
                    modifier = Modifier.widthIn(min = 200.dp)
                ) {
                    Text(
                        text = stringResource(R.string.nuova_partita),
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (prefs?.soundEnabled == true) soundAndMusicPlayer.playSoundEffect(R.raw.button_click_sound)
                        onContinueGameClick()
                    },
                    modifier = Modifier.widthIn(min = 200.dp)
                ) {
                    Text(
                        text = stringResource(R.string.continua_partita),
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (prefs?.soundEnabled == true) soundAndMusicPlayer.playSoundEffect(R.raw.button_click_sound)
                        onViewStatisticsClick()
                    },
                    modifier = Modifier.widthIn(min = 200.dp)
                ) {
                    Text(
                        text = stringResource(R.string.statistiche_di_gioco),
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}
