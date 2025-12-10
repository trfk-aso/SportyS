package com.example.sportys

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sportys.repository.FeatureRepository
import com.example.sportys.repository.FootballRepository
import com.example.sportys.repository.ThemeRepository
import com.example.sportys.screens.Screen
import com.example.sportys.screens.about.AboutScreen
import com.example.sportys.screens.details.DetailsScreen
import com.example.sportys.screens.favorites.FavoritesScreen
import com.example.sportys.screens.history.HistoryScreen
import com.example.sportys.screens.home.HomeScreen
import com.example.sportys.screens.onboarding.OnboardingScreen
import com.example.sportys.screens.search.SearchResultsScreen
import com.example.sportys.screens.search.SearchScreen
import com.example.sportys.screens.settings.AppTheme
import com.example.sportys.screens.settings.SettingsScreen
import com.example.sportys.screens.splash.SplashScreen
import com.example.sportys.screens.statistics.StatisticsScreen
import kotlinx.datetime.Clock
import org.koin.compose.koinInject

@Composable
fun App() {

    val footballRepository: FootballRepository = koinInject()
    val themeRepo: ThemeRepository = koinInject()
    val featureRepo: FeatureRepository = koinInject()

    val navController = rememberNavController()

    val currentThemeId by themeRepo.currentThemeId.collectAsState()

    val selectedTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId }
        ?: AppTheme.LIGHT

    LaunchedEffect(Unit) {
        themeRepo.initializeThemes()
        featureRepo.initializeFeatures()

        val today = Clock.System.now()
        footballRepository.refreshMatchesForDate(today)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Onboarding.route) { OnboardingScreen(navController) }
        composable(Screen.Home.route) {
            HomeScreen(
                navController,
                theme = selectedTheme
            ) }

        composable(Screen.Search.route) {
            SearchScreen(
                onOpenResults = {
                    navController.navigate(Screen.SearchResults.route)
                },
                navController = navController,
                theme = selectedTheme
            )
        }

        composable(Screen.SearchResults.route) {
            SearchResultsScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                theme = selectedTheme
            )
        }

        composable(
            route = "details/{type}/{id}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("id") { type = NavType.StringType }
            )
        ) {
            DetailsScreen(navController, theme = selectedTheme)
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                navController,
                theme = selectedTheme
            ) }
        composable(Screen.History.route) {
            HistoryScreen(
                navController,
                theme = selectedTheme
            )
        }

        composable(Screen.Setting.route) {
            SettingsScreen(
                navigator = navController,
                theme = selectedTheme
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                navController = navController,
                onTeamClick = { teamId ->
                    navController.navigate("details/team/$teamId")
                },
                theme = selectedTheme
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                theme = selectedTheme,
                onBack = { navController.popBackStack() }
            )
        }
    }
}