package com.fourcut.photo.feature.download

internal fun buildPickedPreviewMedia(
    uri: String,
    mimeType: String?
): PreviewMedia? {
    val normalizedMimeType = mimeType?.lowercase()?.takeIf {
        it.startsWith("image/") || it.startsWith("video/")
    } ?: return null
    val extension = when (normalizedMimeType) {
        "image/jpeg" -> "jpg"
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        "video/mp4" -> "mp4"
        "video/quicktime" -> "mov"
        "video/webm" -> "webm"
        else -> if (normalizedMimeType.startsWith("image/")) "img" else "video"
    }
    val prefix = if (normalizedMimeType.startsWith("image/")) "picked-image" else "picked-video"
    return PreviewMedia(
        localPath = uri,
        mimeType = normalizedMimeType,
        fileName = "$prefix.$extension"
    )
}

internal fun addPickedPreviewMedia(
    current: List<PreviewMedia>,
    picked: List<PreviewMedia>
): List<PreviewMedia> {
    return picked.fold(current) { acc, item ->
        addCapturedPreviewMedia(acc, item)
    }
}
