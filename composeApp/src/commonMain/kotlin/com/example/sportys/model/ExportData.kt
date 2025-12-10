package com.example.sportys.model

import kotlinx.serialization.Serializable

@Serializable
data class ExportData(
    val favorites: List<FavoriteItem>,
    val history: List<HistoryItem>,
    val preferences: UserPreferences
)