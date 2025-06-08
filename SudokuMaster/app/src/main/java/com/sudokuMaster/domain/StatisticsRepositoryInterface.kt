package com.sudokuMaster.domain

interface StatisticsRepositoryInterface {
    suspend fun getStatistics(
        onSuccess: (UserStatistics) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun updateStatistic(
        time: Long,
        diff: Difficulty,
        onSuccess: (isRecord: Boolean) -> Unit,
        onError: (Exception) -> Unit
    )
}