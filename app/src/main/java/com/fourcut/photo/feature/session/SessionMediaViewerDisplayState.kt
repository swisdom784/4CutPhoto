package com.fourcut.photo.feature.session

data class SessionMediaViewerDisplayState(
    val imageModel: String?,
    val showImage: Boolean,
    val showVideoThumbnail: Boolean,
    val showVideoFallback: Boolean
)

fun buildSessionMediaViewerDisplayState(
    state: SessionMediaViewerState,
    videoThumbnailPath: String?
): SessionMediaViewerDisplayState {
    return when (state.type) {
        SessionMediaViewerType.Image -> SessionMediaViewerDisplayState(
            imageModel = state.media.path.takeIf { it.isNotBlank() },
            showImage = state.media.path.isNotBlank(),
            showVideoThumbnail = false,
            showVideoFallback = false
        )
        SessionMediaViewerType.Video -> SessionMediaViewerDisplayState(
            imageModel = videoThumbnailPath,
            showImage = false,
            showVideoThumbnail = !videoThumbnailPath.isNullOrBlank(),
            showVideoFallback = videoThumbnailPath.isNullOrBlank()
        )
    }
}
