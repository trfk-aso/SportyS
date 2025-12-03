package com.example.sportys.di

import com.example.sportys.screens.home.HomeViewModel
import com.example.sportys.screens.splash.SplashViewModel
import com.russhwolf.settings.Settings
import org.koin.dsl.module

val viewModule = module {
    single { Settings() }
    single { SplashViewModel(get()) }
    single { HomeViewModel(get()) }
}