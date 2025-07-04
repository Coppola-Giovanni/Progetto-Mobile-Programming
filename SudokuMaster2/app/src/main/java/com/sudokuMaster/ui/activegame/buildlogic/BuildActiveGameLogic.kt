package com.sudokuMaster.ui.activegame.buildlogic

import android.content.Context
import com.sudokuMaster.common.ProductionDispatcherProvider
import com.sudokuMaster.data.repository.GameRepositoryImpl
import com.sudokuMaster.ui.activegame.ActiveGameContainer
import com.sudokuMaster.ui.activegame.ActiveGameLogic
import com.sudokuMaster.ui.activegame.ActiveGameViewModel

internal fun buildActiveGameLogic(
    container: ActiveGameContainer,
    viewModel: ActiveGameViewModel,
    context: Context
): ActiveGameLogic {
    return ActiveGameLogic(
        container,
        viewModel,
        GameRepositoryImpl(                                                //tutte funzioni da lui create per il file system da sostituire
            LocalGameStorageImpl(context.filesDir.path),
            LocalSettingsStorageImpl(context.settingsDataStore)
        ),
        LocalStatisticsStorageImpl(
            context.statsDataStore
        ),
        ProductionDispatcherProvider
    )
}