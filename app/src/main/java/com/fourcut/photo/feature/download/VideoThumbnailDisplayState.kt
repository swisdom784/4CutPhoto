package com.fourcut.photo.feature.download

internal data class VideoThumbnailDisplayState(
    val imageModel: String?,
    val shouldGenerateThumbnail: Boolean,
    val showThumbnailImage: Boolean,
    val showFallbackTile: Boolean
)

internal fun buildVideoThumbnailDisplayState(
    source: String,
    mimeType: String,
    thumbnailPath: String?
): VideoThumbnailDisplayState {
    val isVideo = mimeType.startsWith("video/")
    val canGenerate = isVideo && canGenerateThumbnailFromSource(source)
    val hasThumbnail = !thumbnailPath.isNullOrBlank()
    return VideoThumbnailDisplayState(
        imageModel = thumbnailPath,
        shouldGenerateThumbnail = canGenerate && !hasThumbnail,
        showThumbnailImage = isVideo && hasThumbnail,
        showFallbackTile = isVideo && !hasThumbnail
    )
}

internal fun canGenerateThumbnailFromSource(source: String): Boolean {
    if (source.startsWith("http://") || source.startsWith("https://")) return false
    return source.startsWith("content://") || source.startsWith("/") || source.contains(":")
}
