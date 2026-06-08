package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewDomMediaCandidateRankingTest {
    @Test
    fun promoLogoImageGetsLowScore() {
        val candidate = buildWebViewDomMediaCandidates(
            listOf(
                WebViewDomMediaCandidateRaw(
                    tag = "img",
                    url = "https://sample.invalid/assets/app-banner-logo.png",
                    visible = true,
                    inViewport = true,
                    naturalWidth = 180,
                    naturalHeight = 60,
                    hint = "app banner logo store"
                )
            )
        ).first()

        assertFalse(candidate.recommended)
        assertTrue(candidate.score < 30)
    }

    @Test
    fun largePhotoLikeImageGetsHighScore() {
        val candidate = buildWebViewDomMediaCandidates(
            listOf(
                WebViewDomMediaCandidateRaw(
                    tag = "img",
                    url = "https://sample.invalid/media/photo-large.jpg",
                    visible = true,
                    inViewport = true,
                    width = 320,
                    height = 480,
                    naturalWidth = 1440,
                    naturalHeight = 2160,
                    hint = "download photo"
                )
            )
        ).first()

        assertTrue(candidate.recommended)
        assertTrue(candidate.score >= 70)
    }

    @Test
    fun videoDownloadCandidateIsRecommendedVideo() {
        val candidate = buildWebViewDomMediaCandidates(
            listOf(
                WebViewDomMediaCandidateRaw(
                    tag = "source",
                    url = "https://example.com/video-download/original.mp4",
                    visible = false,
                    inViewport = false,
                    videoWidth = 1920,
                    videoHeight = 1080,
                    hint = "video download"
                )
            )
        ).first()

        assertTrue(candidate.recommended)
        assertEquals("video/mp4", candidate.mimeType)
        assertFalse(candidate.displaySummary.contains("original.mp4"))
    }

    @Test
    fun recommendedCandidatesComeBeforeOtherCandidates() {
        val candidates = buildWebViewDomMediaCandidates(
            listOf(
                WebViewDomMediaCandidateRaw(
                    tag = "img",
                    url = "https://sample.invalid/assets/app-banner.png",
                    visible = true,
                    inViewport = true,
                    naturalWidth = 200,
                    naturalHeight = 80,
                    hint = "app banner"
                ),
                WebViewDomMediaCandidateRaw(
                    tag = "video",
                    url = "https://sample.invalid/video-download/movie.mp4",
                    visible = false,
                    inViewport = false,
                    videoWidth = 1280,
                    videoHeight = 720,
                    hint = "video download"
                )
            )
        )

        assertTrue(candidates.first().recommended)
        assertEquals("video", candidates.first().tag)
    }
}
