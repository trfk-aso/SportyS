package com.example.sportys

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform