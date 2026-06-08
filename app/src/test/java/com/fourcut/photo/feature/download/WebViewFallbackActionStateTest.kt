package com.fourcut.photo.feature.download

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewFallbackActionStateTest {
    @Test
    fun layoutSuspiciousWithPreviewCandidatesShowsCollectCta() {
        val state = buildWebViewFallbackActionState(
            diagnosticKind = WebViewDiagnosticKind.VisibleButLayoutSuspicious,
            candidates = listOf(
                WebViewDomMediaCandidate(
                    actualUrl = "https://sample.invalid/photo.jpg",
                    displayHost = "sample.invalid",
                    scheme = "https",
                    extension = "jpg",
                    mimeType = "image/jpeg",
                    tag = "img",
                    visible = false,
                    inViewport = false,
                    kind = WebViewDomMediaCandidateKind.Previewable,
                    score = 80,
                    recommended = true,
                    displaySummary = "tag=img, scheme=https, host=sample.invalid, ext=jpg"
                )
            ),
            capturedItemCount = 0,
            canRetry = true,
            canOpenExternalBrowser = true
        )

        assertTrue(state.showCollectDomCandidates)
        assertTrue(state.showExternalBrowser)
        assertTrue(state.message.contains("저장 가능한 미디어 후보"))
    }

    @Test
    fun unsupportedOnlyShowsManualImportCta() {
        val state = buildWebViewFallbackActionState(
            diagnosticKind = WebViewDiagnosticKind.UnsupportedDownload,
            candidates = listOf(
                WebViewDomMediaCandidate(
                    actualUrl = "blob:https://sample.invalid/media",
                    displayHost = "none",
                    scheme = "blob",
                    extension = null,
                    mimeType = "video/*",
                    tag = "video",
                    visible = true,
                    inViewport = true,
                    kind = WebViewDomMediaCandidateKind.Unsupported,
                    displaySummary = "tag=video, scheme=blob"
                )
            ),
            capturedItemCount = 0,
            canRetry = true,
            canOpenExternalBrowser = true
        )

        assertFalse(state.showCollectDomCandidates)
        assertTrue(state.showManualImport)
        assertTrue(state.showExternalBrowser)
    }

    @Test
    fun noCandidatesShowsRetryExternalAndManualImport() {
        val state = buildWebViewFallbackActionState(
            diagnosticKind = WebViewDiagnosticKind.Visible,
            candidates = emptyList(),
            capturedItemCount = 0,
            canRetry = true,
            canOpenExternalBrowser = true
        )

        assertFalse(state.showCollectDomCandidates)
        assertTrue(state.showRetry)
        assertTrue(state.showExternalBrowser)
        assertTrue(state.showManualImport)
    }

    @Test
    fun lowScorePromoImageDoesNotShowCollectCta() {
        val state = buildWebViewFallbackActionState(
            diagnosticKind = WebViewDiagnosticKind.VisibleButLayoutSuspicious,
            candidates = listOf(
                WebViewDomMediaCandidate(
                    actualUrl = "https://sample.invalid/banner.png",
                    displayHost = "sample.invalid",
                    scheme = "https",
                    extension = "png",
                    mimeType = "image/png",
                    tag = "img",
                    visible = true,
                    inViewport = true,
                    kind = WebViewDomMediaCandidateKind.Previewable,
                    score = 10,
                    recommended = false,
                    displaySummary = "tag=img, host=sample.invalid, ext=png"
                )
            ),
            capturedItemCount = 0,
            canRetry = true,
            canOpenExternalBrowser = true
        )

        assertFalse(state.showCollectDomCandidates)
        assertTrue(state.showManualImport)
    }

    @Test
    fun videoOnlyCandidateShowsManualImportForMissingPhoto() {
        val state = buildWebViewFallbackActionState(
            diagnosticKind = WebViewDiagnosticKind.VisibleButLayoutSuspicious,
            candidates = listOf(
                WebViewDomMediaCandidate(
                    actualUrl = "https://sample.invalid/video-download/movie.mp4",
                    displayHost = "sample.invalid",
                    scheme = "https",
                    extension = "mp4",
                    mimeType = "video/mp4",
                    tag = "video",
                    visible = false,
                    inViewport = false,
                    kind = WebViewDomMediaCandidateKind.Previewable,
                    score = 90,
                    recommended = true,
                    displaySummary = "tag=video, host=sample.invalid, ext=mp4"
                )
            ),
            capturedItemCount = 0,
            canRetry = true,
            canOpenExternalBrowser = true
        )

        assertTrue(state.showCollectDomCandidates)
        assertTrue(state.showManualImport)
        assertTrue(state.message.contains("영상 후보만"))
    }
}
