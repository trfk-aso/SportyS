package com.example.sportys.model

data class UserPreferences(
    val favoriteLeagueIds: List<String>,
    val favoriteTeamIds: List<String>,
    val isDarkThemeEnabled: Boolean,
    val isDarkThemePurchased: Boolean,
    val isDataResetPurchased: Boolean,
    val language: String?,
    val primaryCountry: String?
)