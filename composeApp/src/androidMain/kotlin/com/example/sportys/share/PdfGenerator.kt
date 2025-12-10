package com.example.sportys.share

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream

object PdfGenerator {
    fun generatePdf(title: String, content: String, context: Context): ByteArray {
        val document = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            textSize = 18f
            isAntiAlias = true
        }

        canvas.drawText(title, 40f, 60f, paint)
        canvas.drawText(content, 40f, 120f, paint)

        document.finishPage(page)

        val stream = ByteArrayOutputStream()
        document.writeTo(stream)
        document.close()

        return stream.toByteArray()
    }
}