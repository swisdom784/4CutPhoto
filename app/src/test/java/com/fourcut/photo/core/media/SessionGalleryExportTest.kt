package com.fourcut.photo.core.media

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class SessionGalleryExportTest {
    @Test
    fun exportSessionMediaExportsExistingFilesAndSkipsMissingFiles() {
        val existing = File.createTempFile("fourcut", ".jpg").apply {
            writeText("image bytes")
            deleteOnExit()
        }
        val exportedNames = mutableListOf<String>()
        val exporter = object : GalleryExporter {
            override fun export(
                sourceFile: File,
                fileName: String,
                mimeType: String
            ): SystemGalleryExportResult {
                exportedNames.add(fileName)
                return SystemGalleryExportResult(
                    uri = Uri.parse("content://media/$fileName"),
                    target = planSystemGalleryExport(fileName, mimeType)
                )
            }
        }

        val summary = exportSessionMediaToSystemGallery(
            media = listOf(
                SystemGalleryExportMedia(existing.path, "image_001.jpg", "image/jpeg"),
                SystemGalleryExportMedia("missing.jpg", "missing.jpg", "image/jpeg")
            ),
            exporter = exporter
        )

        assertEquals(listOf("image_001.jpg"), exportedNames)
        assertEquals(1, summary.exportedCount)
        assertEquals(1, summary.skippedMissingCount)
    }
}
