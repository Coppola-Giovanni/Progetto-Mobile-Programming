package com.sudokuMaster.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.SudokuMaster.R
import com.sudokuMaster.ui.theme.lightGrey
import com.sudokuMaster.ui.theme.mainTitle
import androidx.compose.material3.Text


@Composable
fun LoadingScreen(){
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxHeight(.8f)
            .fillMaxWidth()
    ){
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                modifier = Modifier.size(128.dp),
                contentDescription = stringResource(id = R.string.logo_description)
            )

            LinearProgressIndicator(
                color = lightGrey,
                modifier = Modifier.width(128.dp)
                    .padding(16.dp)
            )

            Text(
                text = stringResource(id = R.string.loading),
                style = mainTitle.copy(color = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.wrapContentSize()
            )

        }
    }
}
