package com.fourcut.photo.core.media

enum class SystemGalleryCollection {
    Pictures,
    Movies
}

data class SystemGalleryExportTarget(
    val collection: SystemGalleryCollection,
    val relativePath: String,
    val displayName: String,
    val mimeType: String
)

fun planSystemGalleryExport(
    fileName: String,
    mimeType: String
): SystemGalleryExportTarget {
    val collection = if (mimeType.startsWith("video/")) {
        SystemGalleryCollection.Movies
    } else {
        SystemGalleryCollection.Pictures
    }
    val relativeRoot = when (collection) {
        SystemGalleryCollection.Pictures -> "Pictures"
        SystemGalleryCollection.Movies -> "Movies"
    }
    val displayName = fileName
        .trim()
        .replace('/', '_')
        .replace('\\', '_')
        .ifBlank {
            when (collection) {
                SystemGalleryCollection.Pictures -> "fourcut_photo.jpg"
                SystemGalleryCollection.Movies -> "fourcut_video.mp4"
            }
        }

    return SystemGalleryExportTarget(
        collection = collection,
        relativePath = "$relativeRoot/4CutPhoto",
        displayName = displayName,
        mimeType = mimeType
    )
}
