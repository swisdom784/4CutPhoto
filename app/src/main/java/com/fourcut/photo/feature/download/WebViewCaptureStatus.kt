package com.fourcut.photo.feature.download

internal sealed interface WebViewCaptureStatus {
    data object Waiting : WebViewCaptureStatus
    data class Captured(val itemCount: Int) : WebViewCaptureStatus
    data object IgnoredUnsupported : WebViewCaptureStatus
}

internal fun webViewCaptureStatusMessage(status: WebViewCaptureStatus): String {
    return when (status) {
        WebViewCaptureStatus.Waiting -> "Tap the photo or video download button on the source page."
        is WebViewCaptureStatus.Captured -> {
            val itemLabel = if (status.itemCount == 1) "item" else "items"
            "Captured ${status.itemCount} $itemLabel. Continue selecting downloads or review the session."
        }
        WebViewCaptureStatus.IgnoredUnsupported -> "That download was not a photo or video."
    }
}
