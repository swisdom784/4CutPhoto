package com.fourcut.photo.core.download

import java.io.InputStream
import java.net.URL
import java.net.URLConnection

private const val REMOTE_MEDIA_CONNECT_TIMEOUT_MILLIS = 15_000
private const val REMOTE_MEDIA_READ_TIMEOUT_MILLIS = 30_000

internal fun URLConnection.applyRemoteMediaDownloadTimeouts(): URLConnection {
    connectTimeout = REMOTE_MEDIA_CONNECT_TIMEOUT_MILLIS
    readTimeout = REMOTE_MEDIA_READ_TIMEOUT_MILLIS
    return this
}

fun URL.openRemoteMediaStream(): InputStream {
    return openConnection()
        .applyRemoteMediaDownloadTimeouts()
        .getInputStream()
}
