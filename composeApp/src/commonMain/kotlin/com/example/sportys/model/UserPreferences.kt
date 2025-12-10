package com.example.sportys.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val favoriteLeagueIds: List<String>,
    val favoriteTeamIds: List<String>,
    val isDarkThemeEnabled: Boolean,
    val isDarkThemePurchased: Boolean,
    val isDataResetPurchased: Boolean,
    val language: String?,
    val primaryCountry: String?,
    val lastMatchesRefresh: String?
)