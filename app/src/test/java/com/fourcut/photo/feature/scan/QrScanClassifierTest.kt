package com.fourcut.photo.feature.scan

import org.junit.Assert.assertEquals
import org.junit.Test

class QrScanClassifierTest {
    @Test
    fun httpUrlIsAccepted() {
        val result = classifyQrScan(
            rawValue = "https://example.com/photo",
            lastAcceptedValue = null,
            isProcessing = false
        )

        assertEquals(QrScanResult.AcceptedUrl("https://example.com/photo"), result)
    }

    @Test
    fun nonUrlQrIsUnsupported() {
        val result = classifyQrScan(
            rawValue = "hello",
            lastAcceptedValue = null,
            isProcessing = false
        )

        assertEquals(QrScanResult.Unsupported("hello"), result)
    }

    @Test
    fun sameUrlWhileProcessingIsIgnoredAsDuplicate() {
        val result = classifyQrScan(
            rawValue = "https://example.com/photo",
            lastAcceptedValue = "https://example.com/photo",
            isProcessing = true
        )

        assertEquals(QrScanResult.DuplicateIgnored, result)
    }

    @Test
    fun blankQrIsIgnored() {
        val result = classifyQrScan(
            rawValue = " ",
            lastAcceptedValue = null,
            isProcessing = false
        )

        assertEquals(QrScanResult.Ignored, result)
    }
}
