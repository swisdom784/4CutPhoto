package com.fourcut.photo.feature.scan

data class FocusTarget(
    val x: Float,
    val y: Float
)

data class ScanFrame(
    val left: Float,
    val top: Float,
    val size: Float
) {
    val right: Float = left + size
    val bottom: Float = top + size
    val center: FocusTarget = FocusTarget(
        x = left + (size / 2f),
        y = top + (size / 2f)
    )
}

data class ScanFocusPolicy(
    val requestCooldownMillis: Long = 1_200L,
    val autoRetryIntervalMillis: Long = 2_500L
) {
    fun shouldRequestAutoFocus(
        nowMillis: Long,
        lastFocusRequestMillis: Long?,
        isScanLocked: Boolean
    ): Boolean {
        if (isScanLocked) return false
        if (lastFocusRequestMillis == null) return true
        return nowMillis - lastFocusRequestMillis >= autoRetryIntervalMillis
    }

    fun canRequestManualFocus(
        nowMillis: Long,
        lastFocusRequestMillis: Long?,
        isScanLocked: Boolean
    ): Boolean {
        if (isScanLocked) return false
        if (lastFocusRequestMillis == null) return true
        return nowMillis - lastFocusRequestMillis >= requestCooldownMillis
    }
}

fun calculateScanFrame(
    containerWidthPx: Float,
    containerHeightPx: Float,
    density: Float
): ScanFrame {
    val containerWidthDp = containerWidthPx / density
    val scanSizeDp = if (containerWidthDp < 340f) {
        containerWidthDp - 48f
    } else {
        292f
    }
    val scanSizePx = scanSizeDp * density
    val left = (containerWidthPx - scanSizePx) / 2f
    val top = (containerHeightPx - scanSizePx) / 2f
    return ScanFrame(left = left, top = top, size = scanSizePx)
}

fun resolveFocusTarget(
    tapX: Float?,
    tapY: Float?,
    frame: ScanFrame
): FocusTarget {
    return if (tapX != null && tapY != null) {
        FocusTarget(x = tapX, y = tapY)
    } else {
        frame.center
    }
}
