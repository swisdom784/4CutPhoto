package com.fourcut.photo.feature.download

data class WebViewDownloadPageConfig(
    val javaScriptEnabled: Boolean,
    val domStorageEnabled: Boolean,
    val supportMultipleWindows: Boolean,
    val javaScriptCanOpenWindowsAutomatically: Boolean,
    val loadWithOverviewMode: Boolean,
    val useWideViewPort: Boolean,
    val keepNavigationInsideWebView: Boolean,
    val allowMixedContentCompatibilityMode: Boolean
)

fun defaultWebViewDownloadPageConfig(): WebViewDownloadPageConfig {
    return WebViewDownloadPageConfig(
        javaScriptEnabled = true,
        domStorageEnabled = true,
        supportMultipleWindows = true,
        javaScriptCanOpenWindowsAutomatically = true,
        loadWithOverviewMode = true,
        useWideViewPort = true,
        keepNavigationInsideWebView = true,
        allowMixedContentCompatibilityMode = true
    )
}
