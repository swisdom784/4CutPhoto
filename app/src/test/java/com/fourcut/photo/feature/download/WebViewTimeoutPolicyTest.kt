package com.fourcut.photo.feature.download

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewTimeoutPolicyTest {
    private val policy = WebViewDiagnosticTimeoutPolicy(timeoutMillis = 10_000L)

    @Test
    fun startedBeforeThresholdDoesNotTimeout() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download",
            nowMillis = 1_000L
        ).copy(kind = WebViewDiagnosticKind.Started, lastEventAtMillis = 2_000L)

        assertFalse(policy.shouldTimeout(state, nowMillis = 11_999L))
    }

    @Test
    fun startedAfterThresholdTimesOut() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download",
            nowMillis = 1_000L
        ).copy(kind = WebViewDiagnosticKind.Started, lastEventAtMillis = 2_000L)

        assertTrue(policy.shouldTimeout(state, nowMillis = 12_000L))
    }

    @Test
    fun visibleStateDoesNotTimeout() {
        val state = WebViewDiagnosticState.initial(
            sourceUrl = "https://sample.invalid/download",
            nowMillis = 1_000L
        ).copy(kind = WebViewDiagnosticKind.Visible, pageVisible = true, lastEventAtMillis = 2_000L)

        assertFalse(policy.shouldTimeout(state, nowMillis = 30_000L))
    }
}
