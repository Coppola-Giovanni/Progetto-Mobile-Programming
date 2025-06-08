package com.sudokuMaster.domain

interface SettingStorageInterface {
    suspend fun getSettings() : SettingsStorageResult
    suspend fun updateSettings(settings: Settings) : SettingsStorageResult
}

sealed class SettingsStorageResult {
    data class onSuccess(val settings: Settings): SettingsStorageResult()
    data class onError(val exception: Exception): SettingsStorageResult()
}