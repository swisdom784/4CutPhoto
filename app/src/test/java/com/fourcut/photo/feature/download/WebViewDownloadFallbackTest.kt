package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewDownloadFallbackTest {
    @Test
    fun timeoutStatusShowsRetryAndExternalBrowserGuidance() {
        val status = webViewPageStateReducer(
            current = WebViewPageStatus.PageStarted,
            event = WebViewPageEvent.Timeout
        )

        assertEquals(WebViewPageStatus.Timeout, status)
        assertEquals(
            "페이지 응답이 지연되고 있어요. 다시 시도하거나 외부 브라우저로 열어보세요.",
            webViewPageStatusMessage(status)
        )
        assertTrue(webViewPageCanRetry(status))
        assertTrue(webViewPageCanOpenExternalBrowser(status))
    }

    @Test
    fun renderGoneStatusExplainsThatPageCannotBeDisplayed() {
        val status = webViewPageStateReducer(
            current = WebViewPageStatus.PageVisible,
            event = WebViewPageEvent.RenderGone
        )

        assertEquals(WebViewPageStatus.RenderGone, status)
        assertEquals(
            "페이지를 표시하지 못했어요. 다시 시도하거나 외부 브라우저로 열어보세요.",
            webViewPageStatusMessage(status)
        )
    }

    @Test
    fun pageCommitVisibleMarksPageVisibleAndClearsLoadingMessage() {
        val status = webViewPageStateReducer(
            current = WebViewPageStatus.PageStarted,
            event = WebViewPageEvent.PageCommitVisible
        )

        assertEquals(WebViewPageStatus.PageVisible, status)
        assertEquals(null, webViewPageStatusMessage(status))
    }

    @Test
    fun retryResetsPageStateToLoading() {
        val status = webViewPageStateReducer(
            current = WebViewPageStatus.Timeout,
            event = WebViewPageEvent.Retry
        )

        assertEquals(WebViewPageStatus.Loading, status)
    }

    @Test
    fun loadedStateDoesNotShowRetryControls() {
        assertFalse(webViewPageCanRetry(WebViewPageStatus.PageVisible))
        assertFalse(webViewPageCanOpenExternalBrowser(WebViewPageStatus.PageVisible))
    }
}
