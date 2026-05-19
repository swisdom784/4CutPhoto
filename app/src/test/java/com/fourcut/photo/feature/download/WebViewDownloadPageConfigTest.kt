package com.fourcut.photo.feature.download

import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewDownloadPageConfigTest {
    @Test
    fun defaultConfigSupportsPhotoBoothDownloadPages() {
        val config = defaultWebViewDownloadPageConfig()

        assertTrue(config.javaScriptEnabled)
        assertTrue(config.domStorageEnabled)
        assertTrue(config.supportMultipleWindows)
        assertTrue(config.javaScriptCanOpenWindowsAutomatically)
        assertTrue(config.loadWithOverviewMode)
        assertTrue(config.useWideViewPort)
        assertTrue(config.keepNavigationInsideWebView)
        assertTrue(config.allowMixedContentCompatibilityMode)
    }
}
