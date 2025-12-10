package com.example.sportys.di

import com.example.sportys.billing.AndroidBillingRepository
import com.example.sportys.billing.BillingRepository
import com.example.sportys.data.AndroidDatabaseDriverFactory
import com.example.sportys.data.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(androidContext()) }
    single<BillingRepository> { AndroidBillingRepository(androidContext(), get(), get()) }
}