package com.fourcut.photo.core.download

import java.net.URI

class DownloadResolver {
    suspend fun resolve(url: String): DownloadResult {
        val uri = runCatching { URI(url) }.getOrNull()
            ?: return DownloadResult.Unsupported("Invalid URL")

        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") {
            return DownloadResult.Unsupported("Only HTTP and HTTPS URLs are supported.")
        }

        val extension = url.substringBefore('?')
            .substringBefore('#')
            .substringAfterLast('.', missingDelimiterValue = "")
            .lowercase()

        val mimeType = mediaMimeTypes[extension]
        return if (mimeType != null) {
            DownloadResult.Automatic(
                items = listOf(
                    DownloadableMedia(
                        url = url,
                        mimeType = mimeType,
                        suggestedFileName = suggestedFileName(uri.path, extension)
                    )
                )
            )
        } else {
            DownloadResult.NeedsWebView(url)
        }
    }

    private fun suggestedFileName(path: String?, extension: String): String {
        val fromPath = path
            ?.substringAfterLast('/')
            ?.takeIf { it.isNotBlank() && it.contains('.') }

        return fromPath ?: "download.$extension"
    }

    private companion object {
        val mediaMimeTypes = mapOf(
            "jpg" to "image/jpeg",
            "jpeg" to "image/jpeg",
            "png" to "image/png",
            "webp" to "image/webp",
            "mp4" to "video/mp4",
            "mov" to "video/quicktime"
        )
    }
}
