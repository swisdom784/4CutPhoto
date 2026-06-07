package com.fourcut.photo.feature.scan

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanFocusPolicyTest {
    private val policy = ScanFocusPolicy(
        requestCooldownMillis = 1_200L,
        autoRetryIntervalMillis = 2_500L
    )

    @Test
    fun initialAutoFocusIsAllowedWhenScanIsNotLocked() {
        assertTrue(
            policy.shouldRequestAutoFocus(
                nowMillis = 10_000L,
                lastFocusRequestMillis = null,
                isScanLocked = false
            )
        )
    }

    @Test
    fun autoFocusIsBlockedDuringCooldown() {
        assertFalse(
            policy.shouldRequestAutoFocus(
                nowMillis = 10_800L,
                lastFocusRequestMillis = 10_000L,
                isScanLocked = false
            )
        )
    }

    @Test
    fun autoFocusIsAllowedAfterRetryInterval() {
        assertTrue(
            policy.shouldRequestAutoFocus(
                nowMillis = 12_600L,
                lastFocusRequestMillis = 10_000L,
                isScanLocked = false
            )
        )
    }

    @Test
    fun focusRequestsStopAfterQrIsLocked() {
        assertFalse(
            policy.shouldRequestAutoFocus(
                nowMillis = 20_000L,
                lastFocusRequestMillis = null,
                isScanLocked = true
            )
        )
        assertFalse(
            policy.canRequestManualFocus(
                nowMillis = 20_000L,
                lastFocusRequestMillis = 17_000L,
                isScanLocked = true
            )
        )
    }

    @Test
    fun manualTapFocusUsesCooldownButDoesNotNeedFullRetryInterval() {
        assertFalse(
            policy.canRequestManualFocus(
                nowMillis = 10_500L,
                lastFocusRequestMillis = 10_000L,
                isScanLocked = false
            )
        )
        assertTrue(
            policy.canRequestManualFocus(
                nowMillis = 11_300L,
                lastFocusRequestMillis = 10_000L,
                isScanLocked = false
            )
        )
    }
}
