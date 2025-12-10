package com.example.sportys.share

import android.content.Intent
import android.content.Context
import androidx.core.content.FileProvider
import com.example.sportys.AppContextProvider
import java.io.File

class AndroidShareManager(private val context: Context) : ShareManager {

    override suspend fun exportPdf(
        title: String,
        content: String,
        imageBytes: ByteArray?
    ): ByteArray {

        return PdfGenerator.generatePdf(title, content, context)
    }

    override fun sharePdf(fileName: String, bytes: ByteArray) {
        val file = File(context.cacheDir, fileName)
        file.writeBytes(bytes)

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share PDF"))
    }

    override fun exportJson(jsonText: String): ByteArray {
        return jsonText.toByteArray(Charsets.UTF_8)
    }

    override fun shareJson(fileName: String, bytes: ByteArray) {
        val file = File(context.cacheDir, fileName)
        file.writeBytes(bytes)

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share JSON"))
    }
}

actual fun getShareManager(): ShareManager {
    return AndroidShareManager(AppContextProvider.get())
}