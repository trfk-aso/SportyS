package com.example.sportys.share

import io.ktor.client.HttpClient

expect fun httpClient(): HttpClient


val ktorClient: HttpClient by lazy { httpClient() }