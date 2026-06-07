package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewLayoutDiagnosticTest {
    @Test
    fun bodyHeightZeroWithMediaElementsIsLayoutSuspicious() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download",
            nowMillis = 1_000L
        )
        val visible = reduceWebViewDiagnostic(
            state,
            WebViewDiagnosticEvent.PageCommitVisible(nowMillis = 1_100L)
        )

        val probed = reduceWebViewDiagnostic(
            visible,
            WebViewDiagnosticEvent.JsProbe(
                nowMillis = 1_500L,
                readyState = "complete",
                hasTitle = true,
                titleLength = 10,
                hasBody = true,
                bodyTextLength = 134,
                bodyChildCount = 3,
                documentElementChildCount = 2,
                bodyBackgroundColor = "rgb(0, 0, 0)",
                bodyTextColor = "rgb(255, 255, 255)",
                documentBackgroundColor = "rgb(0, 0, 0)",
                viewportWidth = 320,
                viewportHeight = 640,
                scrollHeight = 640,
                bodyClientWidth = 320,
                bodyClientHeight = 0,
                linkCount = 1,
                buttonCount = 6,
                imageCount = 2,
                videoCount = 1,
                iframeCount = 0,
                formCount = 0,
                scriptCount = 8,
                anchorHostCount = 1,
                focusedElementTag = "BODY",
                isAndroidWebView = true,
                layoutSuspicious = true,
                domCandidateProbeRequired = true,
                mediaCandidateCount = 3,
                previewableCandidateCount = 1,
                unsupportedCandidateCount = 2,
                succeeded = true
            )
        )

        assertEquals(WebViewDiagnosticKind.VisibleButLayoutSuspicious, probed.kind)
        assertTrue(probed.jsLayoutSuspicious == true)
        assertTrue(probed.jsDomCandidateProbeRequired == true)
        assertEquals(3, probed.jsMediaCandidateCount)
    }
}
