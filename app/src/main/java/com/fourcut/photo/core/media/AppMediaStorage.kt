package com.fourcut.photo.core.media

import android.content.Context
import java.io.File
import java.io.InputStream

class AppMediaStorage(
    private val context: Context
) {
    fun saveOriginal(sessionId: Long, fileName: String, input: InputStream): File {
        val targetDirectory = File(sessionDirectory(sessionId), "original")
        targetDirectory.mkdirs()

        val target = File(targetDirectory, sanitizeFileName(fileName))
        input.use { source ->
            target.outputStream().use { destination ->
                source.copyTo(destination)
            }
        }
        return target
    }

    fun sessionDirectory(sessionId: Long): File {
        return File(context.filesDir, "media/sessions/$sessionId")
    }

    fun deleteSession(sessionId: Long) {
        sessionDirectory(sessionId).deleteRecursively()
    }

    private fun sanitizeFileName(fileName: String): String {
        return fileName
            .replace('/', '_')
            .replace('\\', '_')
    }
}
