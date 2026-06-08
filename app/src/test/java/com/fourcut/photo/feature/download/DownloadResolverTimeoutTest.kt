package com.fourcut.photo.feature.download

import com.fourcut.photo.core.download.DownloadResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadResolverTimeoutTest {
    @Test
    fun timeoutFallsBackToWebView() = runTest {
        val result = resolveDownloadWithWebViewTimeout(
            sourceUrl = "https://sample.invalid/qr",
            timeoutMillis = 100L,
            resolve = {
                delay(1_000L)
                DownloadResult.Unsupported("too late")
            }
        )

        assertEquals(DownloadResult.NeedsWebView("https://sample.invalid/qr"), result)
    }

    @Test
    fun resolverResultIsUsedWhenItFinishesBeforeTimeout() = runTest {
        val result = resolveDownloadWithWebViewTimeout(
            sourceUrl = "https://sample.invalid/photo.jpg",
            timeoutMillis = 1_000L,
            resolve = {
                DownloadResult.NeedsWebView(it)
            }
        )

        assertTrue(result is DownloadResult.NeedsWebView)
    }
}
