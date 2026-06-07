package com.fourcut.photo.feature.download

internal data class WebViewFallbackActionState(
    val message: String,
    val candidateSummary: String?,
    val showCollectDomCandidates: Boolean,
    val showRetry: Boolean,
    val showExternalBrowser: Boolean,
    val showManualImport: Boolean
)

internal fun buildWebViewFallbackActionState(
    diagnosticKind: WebViewDiagnosticKind,
    candidates: List<WebViewDomMediaCandidate>,
    capturedItemCount: Int,
    canRetry: Boolean,
    canOpenExternalBrowser: Boolean
): WebViewFallbackActionState {
    val collectableCandidates = candidates.filter { it.isCollectableDomCandidate() }
    val previewableCount = collectableCandidates.size
    val unsupportedCount = candidates.count { it.kind == WebViewDomMediaCandidateKind.Unsupported }
    val imageCount = collectableCandidates.count {
        it.kind == WebViewDomMediaCandidateKind.Previewable && it.mimeType?.startsWith("image/") == true
    }
    val videoCount = collectableCandidates.count {
        it.kind == WebViewDomMediaCandidateKind.Previewable && it.mimeType?.startsWith("video/") == true
    }
    val hasPreviewCandidates = previewableCount > 0
    val hasUnsupportedOnly = previewableCount == 0 && unsupportedCount > 0
    val hasNoCandidates = candidates.isEmpty()
    val layoutSuspicious = diagnosticKind == WebViewDiagnosticKind.VisibleButLayoutSuspicious
    val hasVideoOnly = videoCount > 0 && imageCount == 0

    val message = when {
        hasVideoOnly ->
            "영상 후보만 찾았어요. 사진은 외부 브라우저에서 저장한 뒤 가져올 수 있어요."
        hasPreviewCandidates && layoutSuspicious ->
            "페이지가 화면에 제대로 표시되지 않지만, 저장 가능한 미디어 후보를 찾았어요."
        hasPreviewCandidates ->
            "페이지에서 사진/영상 후보를 찾았어요."
        hasUnsupportedOnly ->
            "앱에서 바로 가져올 수 없는 다운로드 방식이에요."
        hasNoCandidates && capturedItemCount == 0 ->
            "이 페이지에서 저장 가능한 미디어를 찾지 못했어요."
        else ->
            "원본 페이지에서 사진 또는 영상 다운로드 버튼을 눌러주세요."
    }

    return WebViewFallbackActionState(
        message = message,
        candidateSummary = if (hasPreviewCandidates) {
            buildCandidateSummary(imageCount, videoCount)
        } else {
            null
        },
        showCollectDomCandidates = hasPreviewCandidates,
        showRetry = canRetry || hasNoCandidates,
        showExternalBrowser = canOpenExternalBrowser,
        showManualImport = !hasPreviewCandidates || hasUnsupportedOnly || hasVideoOnly
    )
}

internal fun WebViewDomMediaCandidate.isCollectableDomCandidate(): Boolean {
    if (kind != WebViewDomMediaCandidateKind.Previewable) return false
    if (recommended) return true
    return mimeType?.startsWith("video/") == true && score >= 50
}

private fun buildCandidateSummary(imageCount: Int, videoCount: Int): String {
    return buildList {
        if (imageCount > 0) add("사진 ${imageCount}개")
        if (videoCount > 0) add("영상 ${videoCount}개")
    }.joinToString(" · ").ifBlank { "미디어 후보 있음" }
}
