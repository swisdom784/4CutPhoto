package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewDomMediaCandidateTest {
    @Test
    fun httpImageCandidateBecomesPreviewCandidateWithRedactedDisplay() {
        val candidates = buildWebViewDomMediaCandidates(
            listOf(
                WebViewDomMediaCandidateRaw(
                    tag = "img",
                    url = "https://sample.invalid/media/photo.jpg?token=secret",
                    visible = true,
                    inViewport = true,
                    width = 320,
                    height = 240,
                    naturalWidth = 1280,
                    naturalHeight = 960
                )
            )
        )

        assertEquals(1, candidates.size)
        assertEquals(WebViewDomMediaCandidateKind.Previewable, candidates.first().kind)
        assertEquals("https://sample.invalid/media/photo.jpg?token=secret", candidates.first().actualUrl)
        assertEquals("sample.invalid", candidates.first().displayHost)
        assertEquals("jpg", candidates.first().extension)
        assertEquals("image/jpeg", candidates.first().mimeType)
        assertFalse(candidates.first().displaySummary.contains("token"))
    }

    @Test
    fun visibleVideoCandidateInViewportBecomesPreviewCandidate() {
        val candidates = buildWebViewDomMediaCandidates(
            listOf(
                WebViewDomMediaCandidateRaw(
                    tag = "video",
                    url = "https://example.com/video/original.mp4",
                    visible = true,
                    inViewport = true,
                    width = 320,
                    height = 180,
                    videoWidth = 1920,
                    videoHeight = 1080
                )
            )
        )

        assertEquals(WebViewDomMediaCandidateKind.Previewable, candidates.first().kind)
        assertEquals("video/mp4", candidates.first().mimeType)
    }

    @Test
    fun blobAndDataCandidatesAreUnsupported() {
        val candidates = buildWebViewDomMediaCandidates(
            listOf(
                WebViewDomMediaCandidateRaw(tag = "video", url = "blob:https://sample.invalid/abc", visible = true, inViewport = true),
                WebViewDomMediaCandidateRaw(tag = "img", url = "data:image/png;base64,AAAA", visible = true, inViewport = true)
            )
        )

        assertEquals(WebViewDomMediaCandidateKind.Unsupported, candidates[0].kind)
        assertEquals("blob", candidates[0].scheme)
        assertEquals(WebViewDomMediaCandidateKind.Unsupported, candidates[1].kind)
        assertEquals("data", candidates[1].scheme)
    }

    @Test
    fun duplicateActualUrlsAreDeduped() {
        val candidates = buildWebViewDomMediaCandidates(
            listOf(
                WebViewDomMediaCandidateRaw(tag = "img", url = "https://sample.invalid/photo.jpg", visible = true, inViewport = true),
                WebViewDomMediaCandidateRaw(tag = "source", url = "https://sample.invalid/photo.jpg", visible = true, inViewport = true)
            )
        )

        assertEquals(1, candidates.size)
    }

    @Test
    fun hiddenHttpMediaCandidateStillBecomesPreviewCandidate() {
        val candidates = buildWebViewDomMediaCandidates(
            listOf(
                WebViewDomMediaCandidateRaw(
                    tag = "video",
                    url = "https://sample.invalid/media/original.mp4",
                    visible = false,
                    inViewport = false
                )
            )
        )

        assertEquals(WebViewDomMediaCandidateKind.Previewable, candidates.first().kind)
        assertFalse(candidates.first().displaySummary.contains("original.mp4"))
        assertTrue(candidates.first().displaySummary.contains("host=sample.invalid"))
    }
}
