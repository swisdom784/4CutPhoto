package com.fourcut.photo.core.download

sealed interface DownloadResult {
    data class Automatic(val items: List<DownloadableMedia>) : DownloadResult
    data class NeedsWebView(val url: String) : DownloadResult
    data class Unsupported(val reason: String) : DownloadResult
}

data class DownloadableMedia(
    val url: String,
    val mimeType: String,
    val suggestedFileName: String
)
