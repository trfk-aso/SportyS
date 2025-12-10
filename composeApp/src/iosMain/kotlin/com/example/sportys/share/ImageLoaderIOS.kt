package com.example.sportys.share

import platform.Foundation.*
import platform.UIKit.UIImage

class ImageLoaderIOS : ImageLoader {

    override suspend fun load(url: String?): ByteArray? {
        if (url == null) return null

        val nsUrl = NSURL.URLWithString(url) ?: return null
        val data = NSData.dataWithContentsOfURL(nsUrl) ?: return null

        return data.toByteArray()
    }
}

actual fun getImageLoader(): ImageLoader = ImageLoaderIOS()