package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Test

class WebViewCaptureStatusTest {
    @Test
    fun waitingStatusGuidesUserToTapDownloadButton() {
        val message = webViewCaptureStatusMessage(WebViewCaptureStatus.Waiting)

        assertEquals("원본 페이지에서 사진 또는 영상 다운로드 버튼을 눌러주세요.", message)
    }

    @Test
    fun capturedStatusShowsCapturedItemCount() {
        val message = webViewCaptureStatusMessage(WebViewCaptureStatus.Captured(itemCount = 2))

        assertEquals("2개 항목을 담았어요. 더 선택하거나 세션을 검토해주세요.", message)
    }

    @Test
    fun ignoredStatusExplainsUnsupportedDownload() {
        val message = webViewCaptureStatusMessage(WebViewCaptureStatus.IgnoredUnsupported)

        assertEquals("앱에서 바로 저장할 수 없는 방식이에요. 원본 페이지에서 다른 다운로드 버튼을 시도해주세요.", message)
    }

    @Test
    fun emptyCapturedStatusExplainsNoMediaItems() {
        val message = webViewCaptureStatusMessage(WebViewCaptureStatus.EmptyCaptured)

        assertEquals("아직 담은 사진이나 영상이 없어요.", message)
    }
}
