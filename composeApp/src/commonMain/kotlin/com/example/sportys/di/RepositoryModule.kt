package com.example.sportys.di

import com.example.sportys.repository.FeatureRepository
import com.example.sportys.repository.FeatureRepositoryImpl
import com.example.sportys.repository.FootballApi
import com.example.sportys.repository.FootballApiImpl
import com.example.sportys.repository.FootballDbMapper
import com.example.sportys.repository.FootballRepository
import com.example.sportys.repository.FootballRepositoryImpl
import com.example.sportys.repository.SettingsRepository
import com.example.sportys.repository.SettingsRepositoryImpl
import com.example.sportys.repository.ThemeRepository
import com.example.sportys.repository.ThemeRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {

    single { HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }}

    single(named("NEWS_API_KEY")) { "bfaa2639da53b2b7f671942330dc7c41" }
    single(named("FOOTBALL_API_KEY")) { "c248e8c2f602e83f9da56759ca225d2a" }

    single { FootballDbMapper() }

    single<FootballApi> {
        FootballApiImpl(
            client = get(),
            newsApiKey = get(named("NEWS_API_KEY")),
            footballApiKey = get(named("FOOTBALL_API_KEY"))
        )
    }

    single<FootballRepository> { FootballRepositoryImpl(get(), get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<ThemeRepository> { ThemeRepositoryImpl(get()) }
    single<FeatureRepository> { FeatureRepositoryImpl(get()) }
}