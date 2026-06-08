package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewDiagnosticStateTest {
    @Test
    fun startedThenVisibleMarksPageVisible() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download?token=secret",
            nowMillis = 1_000L
        )

        val started = reduceWebViewDiagnostic(
            state = state,
            event = WebViewDiagnosticEvent.PageStarted(nowMillis = 1_100L, isMainFrame = true)
        )
        val visible = reduceWebViewDiagnostic(
            state = started,
            event = WebViewDiagnosticEvent.PageCommitVisible(nowMillis = 1_500L)
        )

        assertEquals(WebViewDiagnosticKind.Visible, visible.kind)
        assertEquals("PageCommitVisible", visible.lastEvent)
        assertTrue(visible.pageVisible)
        assertFalse(visible.timedOut)
    }

    @Test
    fun finishedWithoutVisibleKeepsDiagnosticDistinct() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download",
            nowMillis = 1_000L
        )

        val started = reduceWebViewDiagnostic(
            state = state,
            event = WebViewDiagnosticEvent.PageStarted(nowMillis = 1_100L, isMainFrame = true)
        )
        val finished = reduceWebViewDiagnostic(
            state = started,
            event = WebViewDiagnosticEvent.PageFinished(nowMillis = 1_800L)
        )

        assertEquals(WebViewDiagnosticKind.Finished, finished.kind)
        assertFalse(finished.pageVisible)
        assertEquals("PageFinished", finished.lastEvent)
    }

    @Test
    fun errorAllowsExternalBrowserAndRetry() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download",
            nowMillis = 1_000L
        )

        val error = reduceWebViewDiagnostic(
            state = state,
            event = WebViewDiagnosticEvent.PageError(
                nowMillis = 1_500L,
                isMainFrame = true,
                errorCode = -2
            )
        )

        assertEquals(WebViewDiagnosticKind.Error, error.kind)
        assertTrue(error.externalBrowserAvailable)
        assertTrue(error.retryAvailable)
        assertEquals(-2, error.errorCode)
    }

    @Test
    fun unsupportedDownloadKeepsCapturedCount() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download",
            nowMillis = 1_000L
        ).copy(capturedItemCount = 2)

        val unsupported = reduceWebViewDiagnostic(
            state = state,
            event = WebViewDiagnosticEvent.UnsupportedDownload(nowMillis = 1_500L)
        )

        assertEquals(WebViewDiagnosticKind.UnsupportedDownload, unsupported.kind)
        assertEquals(2, unsupported.capturedItemCount)
        assertFalse(unsupported.downloadCaptured)
    }

    @Test
    fun capturedDownloadIncrementsCount() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download",
            nowMillis = 1_000L
        )

        val captured = reduceWebViewDiagnostic(
            state = state,
            event = WebViewDiagnosticEvent.CapturedDownload(nowMillis = 1_500L)
        )

        assertEquals(WebViewDiagnosticKind.Captured, captured.kind)
        assertEquals(1, captured.capturedItemCount)
        assertTrue(captured.downloadCaptured)
    }

    @Test
    fun httpsSourceAllowsExternalBrowserFromInitialState() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download",
            nowMillis = 1_000L
        )

        assertTrue(state.externalBrowserAvailable)
        assertEquals("https://sample.invalid/download", state.externalBrowserUrl)
        assertEquals("sample.invalid", state.host)
    }

    @Test
    fun malformedSourceDoesNotAllowExternalBrowser() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "not a url",
            nowMillis = 1_000L
        )

        assertFalse(state.externalBrowserAvailable)
        assertEquals(null, state.externalBrowserUrl)
    }

    @Test
    fun visiblePageWithoutDownloadElementsBecomesNoDownloadAction() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download",
            nowMillis = 1_000L
        )
        val visible = reduceWebViewDiagnostic(
            state = state,
            event = WebViewDiagnosticEvent.PageCommitVisible(nowMillis = 1_200L)
        )

        val probed = reduceWebViewDiagnostic(
            state = visible,
            event = WebViewDiagnosticEvent.JsProbe(
                nowMillis = 1_500L,
                readyState = "interactive",
                hasTitle = true,
                titleLength = 12,
                hasBody = true,
                bodyTextLength = 134,
                bodyChildCount = 3,
                documentElementChildCount = 2,
                bodyBackgroundColor = "rgb(0, 0, 0)",
                bodyTextColor = "rgb(255, 255, 255)",
                documentBackgroundColor = "rgb(0, 0, 0)",
                viewportWidth = 360,
                viewportHeight = 640,
                scrollHeight = 640,
                bodyClientWidth = 360,
                bodyClientHeight = 640,
                linkCount = 0,
                buttonCount = 0,
                imageCount = 0,
                videoCount = 0,
                iframeCount = 0,
                formCount = 0,
                scriptCount = 4,
                anchorHostCount = 0,
                focusedElementTag = null,
                isAndroidWebView = true,
                succeeded = true
            )
        )

        assertEquals(WebViewDiagnosticKind.VisibleNoDownloadCaptured, probed.kind)
        assertTrue(probed.externalBrowserAvailable)
        assertTrue(probed.retryAvailable)
    }
}
