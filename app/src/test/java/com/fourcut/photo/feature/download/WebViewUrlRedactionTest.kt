package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WebViewUrlRedactionTest {
    @Test
    fun redactionKeepsOnlyHost() {
        val redacted = redactUrlForWebViewDiagnostic(
            "https://sample.invalid/path/to/page?token=secret&user=private"
        )

        assertEquals("sample.invalid", redacted)
        assertFalse(redacted.contains("token"))
        assertFalse(redacted.contains("secret"))
        assertFalse(redacted.contains("/path"))
    }

    @Test
    fun malformedUrlIsRedactedAsUnknown() {
        val redacted = redactUrlForWebViewDiagnostic("not a url with token=secret")

        assertEquals("unknown", redacted)
    }
}
