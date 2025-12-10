package com.example.sportys.di

import com.example.sportys.billing.BillingRepository
import com.example.sportys.billing.IOSBillingRepository
import com.example.sportys.data.DatabaseDriverFactory
import com.example.sportys.data.IOSDatabaseDriverFactory
import org.koin.dsl.module

actual val platformModule = module {
    single<DatabaseDriverFactory> { IOSDatabaseDriverFactory() }
    single<BillingRepository> { IOSBillingRepository(get(), get()) }
}

object KoinStarter {
    fun start() = initKoin()
}