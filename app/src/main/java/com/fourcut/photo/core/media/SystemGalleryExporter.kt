package com.fourcut.photo.core.media

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File

data class SystemGalleryExportResult(
    val uri: Uri,
    val target: SystemGalleryExportTarget
)

interface GalleryExporter {
    fun export(
        sourceFile: File,
        fileName: String,
        mimeType: String
    ): SystemGalleryExportResult
}

class SystemGalleryExporter(
    private val context: Context
) : GalleryExporter {
    override fun export(
        sourceFile: File,
        fileName: String,
        mimeType: String
    ): SystemGalleryExportResult {
        require(sourceFile.exists()) { "Source file does not exist: ${sourceFile.path}" }

        val target = planSystemGalleryExport(fileName = fileName, mimeType = mimeType)
        val resolver = context.contentResolver
        val uri = resolver.insert(target.contentUri(), target.contentValues())
            ?: error("Could not create MediaStore entry for ${target.displayName}")

        try {
            resolver.openOutputStream(uri)?.use { output ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: error("Could not open output stream for $uri")
        } catch (throwable: Throwable) {
            resolver.delete(uri, null, null)
            throw throwable
        }

        return SystemGalleryExportResult(uri = uri, target = target)
    }

    private fun SystemGalleryExportTarget.contentUri(): Uri {
        return when (collection) {
            SystemGalleryCollection.Pictures -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            SystemGalleryCollection.Movies -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
    }

    private fun SystemGalleryExportTarget.contentValues(): ContentValues {
        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            }
        }
    }
}
