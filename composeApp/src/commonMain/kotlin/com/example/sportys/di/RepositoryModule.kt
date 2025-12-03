package com.example.sportys.di

import com.example.sportys.repository.FootballApi
import com.example.sportys.repository.FootballApiImpl
import com.example.sportys.repository.FootballDbMapper
import com.example.sportys.repository.FootballRepository
import com.example.sportys.repository.FootballRepositoryImpl
import com.example.sportys.repository.SettingsRepository
import com.example.sportys.repository.SettingsRepositoryImpl
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

    single(named("NEWS_API_KEY")) { "5a46be92a32325e5fee2548bbc2694fc" }
    single(named("FOOTBALL_API_KEY")) { "31933c157a28a9ba52cfd6a6a4787f92" }

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
}