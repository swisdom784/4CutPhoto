package com.fourcut.photo.core.download

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URL

class RemoteMediaDownloadTest {
    @Test
    fun remoteMediaConnectionsUseBoundedTimeouts() {
        val connection = URL("https://example.com/photo.jpg").openConnection()

        connection.applyRemoteMediaDownloadTimeouts()

        assertEquals(15_000, connection.connectTimeout)
        assertEquals(30_000, connection.readTimeout)
    }
}
