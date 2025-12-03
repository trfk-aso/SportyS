package com.example.sportys.repository

import com.russhwolf.settings.Settings

interface SettingsRepository {
    suspend fun isFirstLaunch(): Boolean
    suspend fun setFirstLaunch(value: Boolean)
}

class SettingsRepositoryImpl(
    private val settings: Settings
) : SettingsRepository {

    override suspend fun isFirstLaunch(): Boolean {
        return settings.getBoolean("first_launch", true)
    }

    override suspend fun setFirstLaunch(value: Boolean) {
        settings.putBoolean("first_launch", value)
    }
}