package com.example.sportys.repository

import com.russhwolf.settings.Settings

interface SettingsRepository {
    suspend fun isFirstLaunch(): Boolean
    suspend fun setFirstLaunch(value: Boolean)
    suspend fun getRecentSearches(): List<String>
    suspend fun saveRecentSearches(list: List<String>)
}

class SettingsRepositoryImpl(
    private val settings: Settings
) : SettingsRepository {

    private val KEY = "recent_searches"

    override suspend fun isFirstLaunch(): Boolean {
        return settings.getBoolean("first_launch", true)
    }

    override suspend fun setFirstLaunch(value: Boolean) {
        settings.putBoolean("first_launch", value)
    }

    override suspend fun getRecentSearches(): List<String> {
        val raw = settings.getStringOrNull(KEY) ?: return emptyList()
        return raw.split("|||")
    }

    override suspend fun saveRecentSearches(list: List<String>) {
        val raw = list.joinToString("|||")
        settings.putString(KEY, raw)
    }
}