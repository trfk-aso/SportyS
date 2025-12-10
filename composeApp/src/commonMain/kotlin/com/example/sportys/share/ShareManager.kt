package com.example.sportys.share

interface ShareManager {
    suspend fun exportPdf(title: String, content: String, imageBytes: ByteArray?): ByteArray
    fun sharePdf(fileName: String, bytes: ByteArray)

    fun exportJson(jsonText: String): ByteArray
    fun shareJson(fileName: String, bytes: ByteArray)
}

expect fun getShareManager(): ShareManager