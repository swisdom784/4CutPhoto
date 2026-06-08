package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewDiagnosticOverlayStateTest {
    @Test
    fun overlayIsHiddenWhenDebugDiagnosticsAreDisabled() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download",
            nowMillis = 1_000L
        )

        assertNull(buildWebViewDiagnosticOverlayState(state, enabled = false))
    }

    @Test
    fun loadingOverlayShowsHostAndElapsedTime() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download",
            nowMillis = 1_000L
        ).copy(lastEventAtMillis = 2_500L)

        val overlay = buildWebViewDiagnosticOverlayState(state, enabled = true)

        assertEquals("Loading", overlay?.statusLabel)
        assertEquals("sample.invalid", overlay?.host)
        assertEquals(1_500L, overlay?.elapsedMillis)
    }

    @Test
    fun timeoutOverlayShowsRetryAndExternalBrowser() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download",
            nowMillis = 1_000L
        ).copy(
            kind = WebViewDiagnosticKind.Timeout,
            timedOut = true,
            retryAvailable = true,
            externalBrowserAvailable = true
        )

        val overlay = buildWebViewDiagnosticOverlayState(state, enabled = true)

        assertEquals("Timeout", overlay?.statusLabel)
        assertTrue(overlay?.timedOut == true)
        assertTrue(overlay?.externalBrowserAvailable == true)
    }

    @Test
    fun capturedOverlayShowsCapturedCount() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download",
            nowMillis = 1_000L
        ).copy(
            kind = WebViewDiagnosticKind.Captured,
            capturedItemCount = 2,
            downloadCaptured = true
        )

        val overlay = buildWebViewDiagnosticOverlayState(state, enabled = true)

        assertEquals(2, overlay?.capturedItemCount)
        assertTrue(overlay?.downloadCaptured == true)
        assertFalse(overlay?.timedOut == true)
    }
}
