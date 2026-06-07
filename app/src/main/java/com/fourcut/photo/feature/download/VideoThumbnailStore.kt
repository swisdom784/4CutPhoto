package com.fourcut.photo.feature.download

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import java.security.MessageDigest

internal class VideoThumbnailStore(
    private val context: Context
) {
    fun getOrCreateThumbnail(source: String): String? {
        if (!canGenerateThumbnailFromSource(source)) return null
        val target = File(thumbnailDirectory(), buildVideoThumbnailFileName(source))
        if (target.exists() && target.length() > 0L) return target.absolutePath
        val bitmap = createFrame(source) ?: return null
        target.parentFile?.mkdirs()
        return runCatching {
            target.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 86, output)
            }
            target.absolutePath
        }.getOrNull()
    }

    private fun thumbnailDirectory(): File {
        return File(context.cacheDir, "video-thumbnails")
    }

    private fun createFrame(source: String): Bitmap? {
        return runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                if (source.startsWith("content://")) {
                    retriever.setDataSource(context, Uri.parse(source))
                } else {
                    retriever.setDataSource(source)
                }
                retriever.frameAtTime
            } finally {
                retriever.release()
            }
        }.getOrNull()
    }
}

internal fun buildVideoThumbnailFileName(source: String): String {
    return "${buildVideoThumbnailCacheKey(source)}.jpg"
}

internal fun buildVideoThumbnailCacheKey(source: String): String {
    val bytes = MessageDigest.getInstance("SHA-256")
        .digest(source.toByteArray(Charsets.UTF_8))
    return bytes.joinToString(separator = "") { "%02x".format(it) }
}
