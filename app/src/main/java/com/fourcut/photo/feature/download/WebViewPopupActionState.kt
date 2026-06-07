package com.fourcut.photo.feature.download

internal data class WebViewPopupDiagnostic(
    val modalCandidateCount: Int,
    val closeCandidateCount: Int,
    val dominantOverlayCandidateCount: Int
) {
    val hasPopupCandidate: Boolean =
        modalCandidateCount > 0 || dominantOverlayCandidateCount > 0
}

internal data class WebViewPopupActionState(
    val showDismissPopup: Boolean,
    val message: String
)

internal fun buildWebViewPopupActionState(
    popup: WebViewPopupDiagnostic?
): WebViewPopupActionState {
    val showDismiss = popup?.hasPopupCandidate == true && popup.closeCandidateCount > 0
    return WebViewPopupActionState(
        showDismissPopup = showDismiss,
        message = if (showDismiss) {
            "페이지 안내 팝업이 다운로드 버튼을 가리고 있을 수 있어요."
        } else {
            ""
        }
    )
}
