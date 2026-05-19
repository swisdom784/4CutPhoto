package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Test

class WebViewPageStatusTest {
    @Test
    fun loadingStatusShowsPageLoadingMessage() {
        val message = webViewPageStatusMessage(WebViewPageStatus.Loading)

        assertEquals("다운로드 페이지를 여는 중이에요...", message)
    }

    @Test
    fun loadedStatusDoesNotShowMessage() {
        val message = webViewPageStatusMessage(WebViewPageStatus.Loaded)

        assertEquals(null, message)
    }

    @Test
    fun failedStatusShowsRetryGuidance() {
        val message = webViewPageStatusMessage(WebViewPageStatus.Failed)

        assertEquals("페이지를 열지 못했어요. 네트워크 상태를 확인한 뒤 다시 시도해주세요.", message)
    }
}
