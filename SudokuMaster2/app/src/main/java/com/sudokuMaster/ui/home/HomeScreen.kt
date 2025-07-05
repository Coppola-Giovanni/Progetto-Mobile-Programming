package com.sudokuMaster.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.SudokuMaster.R
import com.sudokuMaster.ui.theme.GraphSudokuTheme

@Composable
fun HomeScreen(
    onNewGameClick: () -> Unit,
    onContinueGameClick: () -> Unit,
    onViewStatisticsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sudoku Master",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Button(
            onClick = onNewGameClick,
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            Text(stringResource(R.string.nuova_partita))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onContinueGameClick,
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            Text(stringResource(R.string.continua_partita))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onViewStatisticsClick,
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            Text(stringResource(R.string.statistiche_di_gioco))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GraphSudokuTheme {
        HomeScreen(
            onNewGameClick = {},
            onContinueGameClick = {},
            onViewStatisticsClick = {}
        )
    }
}