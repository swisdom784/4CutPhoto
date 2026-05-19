package com.fourcut.photo.core.media

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class SystemGalleryExporterTest {
    @Test
    fun exportCopiesFileToSystemGallery() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val source = File(context.cacheDir, "image_001.jpg").apply {
            writeText("image bytes")
        }
        val exporter = SystemGalleryExporter(context)

        val result = exporter.export(
            sourceFile = source,
            fileName = "image_001.jpg",
            mimeType = "image/jpeg"
        )

        assertNotNull(result.uri)
        assertTrue(result.target.displayName == "image_001.jpg")
    }
}
