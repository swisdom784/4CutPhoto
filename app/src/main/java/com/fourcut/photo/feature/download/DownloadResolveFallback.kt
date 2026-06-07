package com.fourcut.photo.feature.download

import com.fourcut.photo.core.download.DownloadResult
import kotlinx.coroutines.withTimeoutOrNull

internal suspend fun resolveDownloadWithWebViewTimeout(
    sourceUrl: String,
    timeoutMillis: Long = 2_500L,
    resolve: suspend (String) -> DownloadResult
): DownloadResult {
    return withTimeoutOrNull(timeoutMillis) {
        resolve(sourceUrl)
    } ?: DownloadResult.NeedsWebView(sourceUrl)
}
