package com.fourcut.photo.feature.gallery

enum class GalleryEmptyState {
    NoSessions,
    NoSearchResults
}

fun galleryEmptyState(
    query: String,
    groupCount: Int
): GalleryEmptyState? {
    if (groupCount > 0) return null
    return if (query.isBlank()) {
        GalleryEmptyState.NoSessions
    } else {
        GalleryEmptyState.NoSearchResults
    }
}
