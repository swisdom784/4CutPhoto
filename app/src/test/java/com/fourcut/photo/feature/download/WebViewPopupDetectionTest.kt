package com.fourcut.photo.feature.download

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewPopupDetectionTest {
    @Test
    fun modalWithCloseCandidateShowsDismissPopupCta() {
        val state = buildWebViewPopupActionState(
            popup = WebViewPopupDiagnostic(
                modalCandidateCount = 1,
                closeCandidateCount = 1,
                dominantOverlayCandidateCount = 1
            )
        )

        assertTrue(state.showDismissPopup)
        assertTrue(state.message.contains("페이지 안내 팝업"))
    }

    @Test
    fun modalWithoutCloseCandidateHidesDismissPopupCta() {
        val state = buildWebViewPopupActionState(
            popup = WebViewPopupDiagnostic(
                modalCandidateCount = 1,
                closeCandidateCount = 0,
                dominantOverlayCandidateCount = 1
            )
        )

        assertFalse(state.showDismissPopup)
    }

    @Test
    fun noModalDoesNotShowDismissPopupCta() {
        val state = buildWebViewPopupActionState(
            popup = WebViewPopupDiagnostic(
                modalCandidateCount = 0,
                closeCandidateCount = 0,
                dominantOverlayCandidateCount = 0
            )
        )

        assertFalse(state.showDismissPopup)
    }
}
