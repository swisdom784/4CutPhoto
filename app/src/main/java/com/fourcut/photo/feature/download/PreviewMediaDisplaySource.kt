package com.fourcut.photo.feature.download

internal data class PreviewMediaDisplaySource(
    val model: String?,
    val showImage: Boolean,
    val showVideoTile: Boolean
)

internal fun buildPreviewMediaDisplaySource(
    media: PreviewMedia
): PreviewMediaDisplaySource {
    val isImage = media.mimeType.startsWith("image/")
    val isVideo = media.mimeType.startsWith("video/")
    return PreviewMediaDisplaySource(
        model = media.localPath.takeIf { it.isNotBlank() },
        showImage = isImage && media.localPath.isNotBlank(),
        showVideoTile = isVideo
    )
}
