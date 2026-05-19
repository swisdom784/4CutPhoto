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

        assertEquals("사진이나 영상 파일이 아닌 다운로드예요.", message)
    }
}
