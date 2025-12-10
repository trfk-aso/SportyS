package com.example.sportys.di

import androidx.lifecycle.SavedStateHandle
import com.example.sportys.screens.details.DetailsViewModel
import com.example.sportys.screens.favorites.FavoritesViewModel
import com.example.sportys.screens.history.HistoryViewModel
import com.example.sportys.screens.home.HomeViewModel
import com.example.sportys.screens.search.SearchViewModel
import com.example.sportys.screens.settings.SettingsViewModel
import com.example.sportys.screens.splash.SplashViewModel
import com.example.sportys.screens.statistics.StatisticsViewModel
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModule = module {
    single { Settings() }
    single { SplashViewModel(get()) }
    single { HomeViewModel(get()) }
    viewModel { (handle: SavedStateHandle) ->
        DetailsViewModel(
            repo = get(),
            savedStateHandle = handle
        )
    }
    single { SearchViewModel(get(), get()) }
    single { FavoritesViewModel(get()) }
    single { HistoryViewModel(get()) }
    single { SettingsViewModel(get(), get(), get()) }
    single { StatisticsViewModel(get()) }
}