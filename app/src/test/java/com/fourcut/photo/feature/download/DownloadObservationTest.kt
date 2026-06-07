package com.fourcut.photo.feature.download

import com.fourcut.photo.core.download.DownloadResult
import com.fourcut.photo.core.download.DownloadableMedia
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadObservationTest {
    @Test
    fun automaticResultExplainsDirectMediaUrl() {
        val observation = downloadObservationForResult(
            DownloadResult.Automatic(
                items = listOf(
                    DownloadableMedia(
                        url = "https://sample.invalid/photo.jpg",
                        mimeType = "image/jpeg",
                        suggestedFileName = "photo.jpg"
                    )
                )
            )
        )

        assertEquals(DownloadObservationStage.DirectMediaUrl, observation.stage)
        assertEquals("사진이나 영상 파일 링크를 바로 찾았어요.", observation.userMessage)
    }

    @Test
    fun webViewResultExplainsFallbackPage() {
        val observation = downloadObservationForResult(
            DownloadResult.NeedsWebView("https://sample.invalid/download")
        )

        assertEquals(DownloadObservationStage.WebViewFallback, observation.stage)
        assertEquals("원본 다운로드 페이지를 앱 안에서 열고 있어요.", observation.userMessage)
    }

    @Test
    fun unsupportedResultKeepsUserFacingReason() {
        val observation = downloadObservationForResult(
            DownloadResult.Unsupported("HTTP 또는 HTTPS URL만 지원해요.")
        )

        assertEquals(DownloadObservationStage.Unsupported, observation.stage)
        assertEquals("HTTP 또는 HTTPS URL만 지원해요.", observation.userMessage)
    }
}
