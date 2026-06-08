package com.fourcut.photo.feature.download

import android.util.Log

internal object WebViewDiagnosticRuntime {
    const val isEnabled: Boolean = true

    fun log(state: WebViewDiagnosticState, eventName: String) {
        Log.d(
            "FourCutWebViewDiag",
                "event=$eventName elapsed=${state.lastEventAtMillis - state.startedAtMillis}ms host=${state.host} " +
                    "state=${state.kind} mainFrame=${state.isMainFrame} error=${state.errorCode} http=${state.httpStatusCode} " +
                    "visible=${state.pageVisible} captured=${state.capturedItemCount} timeout=${state.timedOut} " +
                    "external=${state.externalBrowserAvailable} ready=${state.jsReadyState} body=${state.jsHasBody} " +
                    "text=${state.jsBodyTextLength} child=${state.jsBodyChildCount} button=${state.jsButtonCount} " +
                    "image=${state.jsImageCount} video=${state.jsVideoCount} iframe=${state.jsIframeCount} " +
                    "bodySize=${state.jsBodyClientWidth}x${state.jsBodyClientHeight} layout=${state.jsLayoutSuspicious} " +
                    "media=${state.jsMediaCandidateCount} preview=${state.jsPreviewableCandidateCount} " +
                    "unsupported=${state.jsUnsupportedCandidateCount}"
        )
    }
}
