package com.example.sportys.share

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.useContents
import platform.CoreFoundation.CFAttributedStringCreate
import platform.CoreFoundation.CFAttributedStringRef
import platform.CoreFoundation.CFDictionaryCreate
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRangeMake
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFStringRefVar
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreGraphics.*
import platform.CoreText.CTFontCreateUIFontForLanguage
import platform.CoreText.CTFrameDraw
import platform.CoreText.CTFrameGetVisibleStringRange
import platform.CoreText.CTFramesetterCreateFrame
import platform.CoreText.CTFramesetterCreateWithAttributedString
import platform.CoreText.CTFramesetterSuggestFrameSizeWithConstraints
import platform.CoreText.kCTFontAttributeName
import platform.CoreText.kCTFontUIFontSystem
import platform.Foundation.*
import platform.UIKit.*

object PdfGeneratorIOS {

    @OptIn(ExperimentalForeignApi::class)
    fun generatePdf(
        title: String,
        content: String,
        image: UIImage?
    ): NSData {

        val pageWidth = 595.0
        val pageHeight = 842.0
        val padding = 32.0

        val renderer = UIGraphicsPDFRenderer(
            bounds = CGRectMake(0.0, 0.0, pageWidth, pageHeight),
            format = UIGraphicsPDFRendererFormat()
        )

        return renderer.PDFDataWithActions { ctx ->

            ctx?.beginPage()
            var y = padding

            y += drawTitle(title, fontSize = 22.0, x = padding, y = y, maxWidth = pageWidth - padding * 2) + 40

            if (image != null) {
                val imgHeight = 180.0
                image.drawInRect(CGRectMake(padding, y, pageWidth - padding * 2, imgHeight))
                y += imgHeight + 20
            }

            y = drawMultilineText(
                text = content,
                fontSize = 15.0,
                startY = y,
                pageWidth = pageWidth,
                pageHeight = pageHeight,
                padding = padding,
                ctx = ctx!!
            )
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun drawTitle(
        text: String,
        fontSize: Double,
        x: Double,
        y: Double,
        maxWidth: Double
    ): Double {

        val ns = NSString.create(string = text)

        ns.drawInRect(
            CGRectMake(x, y, maxWidth, Double.MAX_VALUE),
            mapOf(
                NSFontAttributeName to UIFont.boldSystemFontOfSize(fontSize)
            )
        )

        val lineHeight = UIFont.systemFontOfSize(fontSize).lineHeight
        val lines = text.split("\n").size

        return lineHeight * lines
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun drawMultilineText(
        text: String,
        fontSize: Double,
        startY: Double,
        pageWidth: Double,
        pageHeight: Double,
        padding: Double,
        ctx: UIGraphicsPDFRendererContext
    ): Double {

        var y = startY

        memScoped {

            val nsString = NSString.create(string = text)
            val cfString = nsString.toCFString()

            val ctFont = CTFontCreateUIFontForLanguage(
                kCTFontUIFontSystem,
                fontSize,
                null
            )

            val attributes = CFDictionaryCreateMutable(
                null,
                1,
                null,
                null
            )
            CFDictionarySetValue(attributes, kCTFontAttributeName, ctFont)

            val attrString = CFAttributedStringCreate(null, cfString, attributes)

            val framesetter = CTFramesetterCreateWithAttributedString(attrString)

            var currentIndex = 0L
            val textLength = nsString.length().toLong()

            while (currentIndex < textLength) {

                val rect = CGRectMake(
                    padding,
                    padding,
                    pageWidth - padding * 2,
                    pageHeight - padding - y
                )

                val path = CGPathCreateMutable()
                CGPathAddRect(path, null, rect)

                val frame = CTFramesetterCreateFrame(
                    framesetter,
                    CFRangeMake(currentIndex, 0),
                    path,
                    null
                )

                val range = CTFrameGetVisibleStringRange(frame)
                val visibleLength = range.useContents { length }.toLong()

                val cg = UIGraphicsGetCurrentContext()

                CGContextSaveGState(cg)
                CGContextTranslateCTM(cg, 0.0, pageHeight)
                CGContextScaleCTM(cg, 1.0, -1.0)

                CTFrameDraw(frame, cg)

                CGContextRestoreGState(cg)

                val size = CTFramesetterSuggestFrameSizeWithConstraints(
                    framesetter,
                    CFRangeMake(currentIndex, visibleLength),
                    null,
                    CGSizeMake(pageWidth - padding * 2, Double.MAX_VALUE),
                    null
                )

                val blockHeight = size.useContents { height }
                y += blockHeight + 12

                currentIndex += visibleLength

                if (currentIndex < textLength) {
                    ctx.beginPage()
                    y = padding
                }
            }
        }

        return y
    }
}

@OptIn(ExperimentalForeignApi::class)
fun NSString.toCFString(): CFStringRef =
    CFBridgingRetain(this) as CFStringRef