package com.example.sportys.share

interface ImageLoader {
    suspend fun load(url: String?): ByteArray?
}

expect fun getImageLoader(): ImageLoader