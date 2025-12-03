package com.example.sportys.di

import com.example.sportys.data.DatabaseDriverFactory
import com.example.sportys.data.IOSDatabaseDriverFactory
import org.koin.dsl.module

actual val platformModule = module {
    single<DatabaseDriverFactory> { IOSDatabaseDriverFactory() }
}

object KoinStarter {
    fun start() = initKoin()
}