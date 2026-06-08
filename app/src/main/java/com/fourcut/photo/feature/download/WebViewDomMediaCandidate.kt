package com.fourcut.photo.feature.download

import java.net.URI

internal enum class WebViewDomMediaCandidateKind {
    Previewable,
    Unsupported,
    Ignored
}

internal data class WebViewDomMediaCandidateRaw(
    val tag: String,
    val url: String?,
    val visible: Boolean,
    val inViewport: Boolean,
    val width: Int? = null,
    val height: Int? = null,
    val naturalWidth: Int? = null,
    val naturalHeight: Int? = null,
    val videoWidth: Int? = null,
    val videoHeight: Int? = null,
    val hint: String? = null
)

internal data class WebViewDomMediaCandidate(
    val actualUrl: String,
    val displayHost: String,
    val scheme: String,
    val extension: String?,
    val mimeType: String?,
    val tag: String,
    val visible: Boolean,
    val inViewport: Boolean,
    val kind: WebViewDomMediaCandidateKind,
    val score: Int = 0,
    val recommended: Boolean = false,
    val displaySummary: String
)

internal fun buildWebViewDomMediaCandidates(
    rawCandidates: List<WebViewDomMediaCandidateRaw>
): List<WebViewDomMediaCandidate> {
    return rawCandidates
        .asSequence()
        .filter { !it.url.isNullOrBlank() }
        .distinctBy { it.url }
        .mapNotNull { raw -> raw.toDomMediaCandidate() }
        .sortedWith(
            compareByDescending<WebViewDomMediaCandidate> { it.recommended }
                .thenByDescending { it.score }
        )
        .toList()
}

private fun WebViewDomMediaCandidateRaw.toDomMediaCandidate(): WebViewDomMediaCandidate? {
    val actual = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val scheme = detectScheme(actual)
    val host = if (scheme == "http" || scheme == "https") {
        runCatching { URI(actual).host }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "unknown"
    } else {
        "none"
    }
    val extension = detectExtension(actual)
    val mimeType = detectMimeType(tag, extension)
    val score = scoreCandidate(
        tag = tag,
        extension = extension,
        mimeType = mimeType,
        visible = visible,
        inViewport = inViewport,
        naturalWidth = naturalWidth,
        naturalHeight = naturalHeight,
        videoWidth = videoWidth,
        videoHeight = videoHeight,
        hint = hint,
        url = actual
    )
    val kind = when {
        scheme == "http" || scheme == "https" -> {
            if (mimeType != null) {
                WebViewDomMediaCandidateKind.Previewable
            } else {
                WebViewDomMediaCandidateKind.Ignored
            }
        }
        scheme == "blob" || scheme == "data" -> WebViewDomMediaCandidateKind.Unsupported
        else -> WebViewDomMediaCandidateKind.Ignored
    }
    val size = listOfNotNull(
        width?.let { w -> height?.let { h -> "${w}x$h" } },
        naturalWidth?.let { w -> naturalHeight?.let { h -> "natural=${w}x$h" } },
        videoWidth?.let { w -> videoHeight?.let { h -> "video=${w}x$h" } }
    ).joinToString(", ").ifBlank { "size=unknown" }

    return WebViewDomMediaCandidate(
        actualUrl = actual,
        displayHost = host,
        scheme = scheme,
        extension = extension,
        mimeType = mimeType,
        tag = tag.lowercase(),
        visible = visible,
        inViewport = inViewport,
        kind = kind,
        score = score,
        recommended = kind == WebViewDomMediaCandidateKind.Previewable && score >= 60,
        displaySummary = "tag=${tag.lowercase()}, scheme=$scheme, host=$host, ext=${extension ?: "unknown"}, type=${mimeType ?: "unknown"}, score=$score, visible=$visible, viewport=$inViewport, $size"
    )
}

private fun detectScheme(url: String): String {
    val index = url.indexOf(':')
    if (index <= 0) return "empty"
    return url.substring(0, index).lowercase()
}

private fun detectExtension(url: String): String? {
    val path = runCatching { URI(url).path }.getOrNull() ?: url.substringBefore('?')
    return path.substringAfterLast('.', missingDelimiterValue = "")
        .lowercase()
        .takeIf { it.isNotBlank() && it.length <= 5 }
}

private fun detectMimeType(tag: String, extension: String?): String? {
    return when (extension) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "mp4" -> "video/mp4"
        "mov" -> "video/quicktime"
        "webm" -> "video/webm"
        else -> when (tag.lowercase()) {
            "img" -> "image/*"
            "video", "source" -> "video/*"
            else -> null
        }
    }
}

private fun scoreCandidate(
    tag: String,
    extension: String?,
    mimeType: String?,
    visible: Boolean,
    inViewport: Boolean,
    naturalWidth: Int?,
    naturalHeight: Int?,
    videoWidth: Int?,
    videoHeight: Int?,
    hint: String?,
    url: String
): Int {
    if (mimeType == null) return 0
    var score = 20
    val normalizedHint = "${hint.orEmpty()} $url".lowercase()
    val badKeywords = listOf("logo", "icon", "banner", "promo", "promotion", "store", "app", "play-store", "app-store")
    val goodKeywords = listOf("download", "photo", "original", "media", "video-download")

    if (mimeType.startsWith("video/")) score += 45
    if (mimeType.startsWith("image/")) score += 20
    if (extension in setOf("jpg", "jpeg", "png", "webp", "mp4", "mov", "webm")) score += 10
    if (visible) score += 5
    if (inViewport) score += 5
    if (goodKeywords.any { normalizedHint.contains(it) }) score += 20
    if (badKeywords.any { normalizedHint.contains(it) }) score -= 45

    val imageLargeEnough = (naturalWidth ?: 0) >= 700 && (naturalHeight ?: 0) >= 700
    val videoLargeEnough = (videoWidth ?: 0) >= 720 && (videoHeight ?: 0) >= 480
    if (imageLargeEnough || videoLargeEnough) score += 25
    if ((naturalWidth ?: 0) in 1..299 || (naturalHeight ?: 0) in 1..299) score -= 20

    return score.coerceIn(0, 100)
}
