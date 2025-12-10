package com.example.sportys.share

class ImageLoaderAndroid : ImageLoader {
    override suspend fun load(url: String?): ByteArray? = null
}

actual fun getImageLoader(): ImageLoader = ImageLoaderAndroid()