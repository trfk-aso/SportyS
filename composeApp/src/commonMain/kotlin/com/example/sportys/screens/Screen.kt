package com.example.sportys.screens

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")

    data object Home : Screen("home")
    data object Search : Screen("search")
    data object History : Screen("history")
    data object Statistics : Screen("statistics")
    data object Favorites : Screen("favorites")
}