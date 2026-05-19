package com.fourcut.photo.core.media

import java.io.File

data class SystemGalleryExportMedia(
    val localPath: String,
    val fileName: String,
    val mimeType: String
)

data class SessionGalleryExportSummary(
    val exportedCount: Int,
    val skippedMissingCount: Int
)

fun exportSessionMediaToSystemGallery(
    media: List<SystemGalleryExportMedia>,
    exporter: GalleryExporter
): SessionGalleryExportSummary {
    var exportedCount = 0
    var skippedMissingCount = 0

    media.forEach { item ->
        val sourceFile = File(item.localPath)
        if (sourceFile.exists()) {
            exporter.export(
                sourceFile = sourceFile,
                fileName = item.fileName,
                mimeType = item.mimeType
            )
            exportedCount += 1
        } else {
            skippedMissingCount += 1
        }
    }

    return SessionGalleryExportSummary(
        exportedCount = exportedCount,
        skippedMissingCount = skippedMissingCount
    )
}
