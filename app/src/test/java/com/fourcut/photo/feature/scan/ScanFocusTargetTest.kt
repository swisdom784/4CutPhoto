package com.fourcut.photo.feature.scan

import org.junit.Assert.assertEquals
import org.junit.Test

class ScanFocusTargetTest {
    @Test
    fun scanFrameCenterUsesDefaultSizeOnRegularWidth() {
        val frame = calculateScanFrame(
            containerWidthPx = 1080f,
            containerHeightPx = 2340f,
            density = 3f
        )

        assertEquals(102f, frame.left, 0.01f)
        assertEquals(732f, frame.top, 0.01f)
        assertEquals(978f, frame.right, 0.01f)
        assertEquals(1608f, frame.bottom, 0.01f)
        assertEquals(540f, frame.center.x, 0.01f)
        assertEquals(1170f, frame.center.y, 0.01f)
    }

    @Test
    fun scanFrameShrinksOnCompactWidth() {
        val frame = calculateScanFrame(
            containerWidthPx = 900f,
            containerHeightPx = 2000f,
            density = 3f
        )

        assertEquals(72f, frame.left, 0.01f)
        assertEquals(622f, frame.top, 0.01f)
        assertEquals(828f, frame.right, 0.01f)
        assertEquals(1378f, frame.bottom, 0.01f)
    }

    @Test
    fun manualTapFocusUsesTappedPoint() {
        val frame = calculateScanFrame(
            containerWidthPx = 1080f,
            containerHeightPx = 2340f,
            density = 3f
        )

        val target = resolveFocusTarget(
            tapX = 200f,
            tapY = 300f,
            frame = frame
        )

        assertEquals(200f, target.x, 0.01f)
        assertEquals(300f, target.y, 0.01f)
    }

    @Test
    fun focusFallsBackToScanFrameCenterWhenTapIsMissing() {
        val frame = calculateScanFrame(
            containerWidthPx = 1080f,
            containerHeightPx = 2340f,
            density = 3f
        )

        val target = resolveFocusTarget(
            tapX = null,
            tapY = null,
            frame = frame
        )

        assertEquals(frame.center.x, target.x, 0.01f)
        assertEquals(frame.center.y, target.y, 0.01f)
    }
}
