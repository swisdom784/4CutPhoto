package com.fourcut.photo.core.download

fun captureWebViewDownload(
    url: String,
    contentDisposition: String?,
    mimeType: String?
): DownloadableMedia? {
    val normalizedMimeType = mimeType
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: mimeTypeForMediaExtension(mediaExtensionFromUrl(url))

    if (normalizedMimeType == null || !normalizedMimeType.isSupportedMediaMimeType()) {
        return null
    }

    return DownloadableMedia(
        url = url,
        mimeType = normalizedMimeType,
        suggestedFileName = fileNameFromContentDisposition(contentDisposition)
            ?: fileNameFromUrl(url)
            ?: fallbackFileName(normalizedMimeType)
    )
}

private fun String.isSupportedMediaMimeType(): Boolean {
    return startsWith("image/") || startsWith("video/")
}

private fun fileNameFromContentDisposition(contentDisposition: String?): String? {
    if (contentDisposition.isNullOrBlank()) return null
    val filenamePart = contentDisposition
        .split(';')
        .map { it.trim() }
        .firstOrNull { it.startsWith("filename=", ignoreCase = true) }
        ?: return null

    return filenamePart
        .substringAfter('=')
        .trim()
        .trim('"')
        .takeIf { it.isNotBlank() }
}

private fun fileNameFromUrl(url: String): String? {
    return url.substringBefore('?')
        .substringBefore('#')
        .substringAfterLast('/')
        .takeIf { it.isNotBlank() && it.contains('.') }
}

private fun fallbackFileName(mimeType: String): String {
    return when {
        mimeType == "image/png" -> "download.png"
        mimeType == "image/webp" -> "download.webp"
        mimeType.startsWith("video/") -> "download.mp4"
        else -> "download.jpg"
    }
}
