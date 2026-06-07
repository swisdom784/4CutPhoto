package com.fourcut.photo.core.download

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadResolverTest {
    @Test
    fun directImageUrlReturnsAutomaticMedia() = runTest {
        val resolver = DownloadResolver()

        val result = resolver.resolve("https://example.com/photo.jpg")

        assertTrue(result is DownloadResult.Automatic)
        assertEquals("https://example.com/photo.jpg", (result as DownloadResult.Automatic).items.first().url)
        assertEquals("image/jpeg", result.items.first().mimeType)
    }

    @Test
    fun directImageUrlWithQueryReturnsAutomaticMedia() = runTest {
        val resolver = DownloadResolver()

        val result = resolver.resolve("https://example.com/photo.jpg?download=sample")

        assertTrue(result is DownloadResult.Automatic)
        assertEquals("image/jpeg", (result as DownloadResult.Automatic).items.first().mimeType)
        assertEquals("photo.jpg", result.items.first().suggestedFileName)
    }

    @Test
    fun directVideoUrlReturnsAutomaticMedia() = runTest {
        val resolver = DownloadResolver()

        val result = resolver.resolve("https://example.com/movie.mp4")

        assertTrue(result is DownloadResult.Automatic)
        assertEquals("video/mp4", (result as DownloadResult.Automatic).items.first().mimeType)
    }

    @Test
    fun uppercaseVideoExtensionReturnsAutomaticMedia() = runTest {
        val resolver = DownloadResolver()

        val result = resolver.resolve("https://example.com/MOVIE.MP4")

        assertTrue(result is DownloadResult.Automatic)
        assertEquals("video/mp4", (result as DownloadResult.Automatic).items.first().mimeType)
    }

    @Test
    fun htmlPageReturnsWebViewFallback() = runTest {
        val resolver = DownloadResolver()

        val result = resolver.resolve("https://example.com/download")

        assertTrue(result is DownloadResult.NeedsWebView)
    }

    @Test
    fun mediaFileOnlyInQueryFallsBackToWebView() = runTest {
        val resolver = DownloadResolver()

        val result = resolver.resolve("https://example.com/download?file=photo.jpg")

        assertTrue(result is DownloadResult.NeedsWebView)
    }

    @Test
    fun malformedUrlReturnsUnsupported() = runTest {
        val resolver = DownloadResolver()

        val result = resolver.resolve("https://sample.invalid/a bad url/photo.jpg")

        assertTrue(result is DownloadResult.Unsupported)
        assertEquals("유효하지 않은 URL이에요.", (result as DownloadResult.Unsupported).reason)
    }

    @Test
    fun nonHttpUrlReturnsUnsupported() = runTest {
        val resolver = DownloadResolver()

        val result = resolver.resolve("mailto:hello@example.com")

        assertTrue(result is DownloadResult.Unsupported)
    }
}
