package com.fourcut.photo.feature.session

enum class SessionMediaViewerType {
    Image,
    Video
}

data class SessionMediaViewerState(
    val media: SessionDetailMediaUiModel,
    val type: SessionMediaViewerType
)

fun openSessionMediaViewer(
    current: SessionMediaViewerState?,
    media: SessionDetailMediaUiModel?
): SessionMediaViewerState? {
    if (media == null) return current
    return SessionMediaViewerState(
        media = media,
        type = if (media.isVideo) SessionMediaViewerType.Video else SessionMediaViewerType.Image
    )
}

fun closeSessionMediaViewer(
    current: SessionMediaViewerState?
): SessionMediaViewerState? = null
