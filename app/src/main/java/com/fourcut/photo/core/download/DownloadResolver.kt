package com.fourcut.photo.core.download

import java.net.URI

class DownloadResolver {
    suspend fun resolve(url: String): DownloadResult {
        val uri = runCatching { URI(url) }.getOrNull()
            ?: return DownloadResult.Unsupported("유효하지 않은 URL이에요.")

        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") {
            return DownloadResult.Unsupported("HTTP 또는 HTTPS URL만 지원해요.")
        }

        val extension = mediaExtensionFromUrl(url)

        val mimeType = mimeTypeForMediaExtension(extension)
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
}

internal fun mediaExtensionFromUrl(url: String): String {
    return url.substringBefore('?')
        .substringBefore('#')
        .substringAfterLast('.', missingDelimiterValue = "")
        .lowercase()
}

internal fun mimeTypeForMediaExtension(extension: String): String? {
    return mediaMimeTypes[extension.lowercase()]
}

private val mediaMimeTypes = mapOf(
    "jpg" to "image/jpeg",
    "jpeg" to "image/jpeg",
    "png" to "image/png",
    "webp" to "image/webp",
    "mp4" to "video/mp4",
    "mov" to "video/quicktime"
)
