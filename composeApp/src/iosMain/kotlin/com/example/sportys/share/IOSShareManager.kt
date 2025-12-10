package com.example.sportys.share

import kotlinx.cinterop.*
import platform.CoreGraphics.CGRectMake
import platform.Foundation.*
import platform.UIKit.*

class IOSShareManager : ShareManager {

    override suspend fun exportPdf(
        title: String,
        content: String,
        imageBytes: ByteArray?
    ): ByteArray {

        val uiImage: UIImage? = imageBytes
            ?.toNSData()
            ?.let { NSData -> UIImage(data = NSData) }

        val pdfData = PdfGeneratorIOS.generatePdf(
            title = title,
            content = content,
            image = uiImage
        )

        return pdfData.toByteArray()
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun sharePdf(fileName: String, bytes: ByteArray) {

        val nsData = bytes.toNSData()

        val temp = NSTemporaryDirectory()
        val url = NSURL.fileURLWithPath(temp).URLByAppendingPathComponent(fileName)
            ?: return

        nsData.writeToURL(url, true)

        val controller = UIActivityViewController(
            activityItems = listOf(url),
            applicationActivities = null
        )

        val root = topViewController() ?: return

        val pop = controller.popoverPresentationController
        pop?.sourceView = root.view
        pop?.sourceRect = CGRectMake(0.0, 0.0, 1.0, 1.0)

        root.presentViewController(controller, true, null)
    }

    override fun exportJson(jsonText: String): ByteArray {
        return jsonText.encodeToByteArray()
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun shareJson(fileName: String, bytes: ByteArray) {

        val nsData = bytes.toNSData()

        val temp = NSTemporaryDirectory()
        val url = NSURL.fileURLWithPath(temp).URLByAppendingPathComponent(fileName)
            ?: return

        nsData.writeToURL(url, true)

        val controller = UIActivityViewController(
            activityItems = listOf(url),
            applicationActivities = null
        )

        val root = topViewController() ?: return
        val pop = controller.popoverPresentationController
        pop?.sourceView = root.view
        pop?.sourceRect = CGRectMake(0.0, 0.0, 1.0, 1.0)

        root.presentViewController(controller, true, null)
    }
}

actual fun getShareManager(): ShareManager = IOSShareManager()

fun topViewController(): UIViewController? {
    val keyWindow = UIApplication.sharedApplication.keyWindow
        ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
        ?: return null

    var top = keyWindow.rootViewController ?: return null

    while (top.presentedViewController != null) {
        top = top.presentedViewController!!
    }

    if (top is UINavigationController) {
        return top.visibleViewController
    }

    if (top is UITabBarController) {
        return top.selectedViewController
    }

    return top
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    val byteArray = ByteArray(length)

    memScoped {
        val ptr = byteArray.refTo(0).getPointer(this)
        this@toByteArray.getBytes(ptr, length.toULong())
    }

    return byteArray
}

@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toNSData(): NSData = memScoped {
    this@toNSData.usePinned { pinned ->
        NSData.create(
            bytes = pinned.addressOf(0),
            length = this@toNSData.size.toULong()
        )
    }
}