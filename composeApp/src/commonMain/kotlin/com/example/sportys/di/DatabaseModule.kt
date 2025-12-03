package com.example.sportys.di

import com.example.sportys.data.DatabaseDriverFactory
import com.example.sportys.data.SportyS
import org.koin.dsl.module

val databaseModule = module {
    single { SportyS(get<DatabaseDriverFactory>().createDriver()) }
}