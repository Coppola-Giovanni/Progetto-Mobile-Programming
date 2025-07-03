package com.sudokuMaster.domain

interface SettingStorageInterface {
    suspend fun getSettings() : SettingsStorageResult
    suspend fun updateSettings(settings: Settings) : SettingsStorageResult
}

sealed class SettingsStorageResult {
    data class OnSuccess(val settings: Settings): SettingsStorageResult()
    object onComplete : SettingsStorageResult()
    data class OnError(val exception: Exception): SettingsStorageResult()
}