package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadSaveStatusTest {
    @Test
    fun savingStatusShowsProgressMessage() {
        val message = downloadSaveStatusMessage(DownloadSaveStatus.Saving)

        assertEquals("세션을 저장하고 있어요...", message)
    }

    @Test
    fun failedStatusShowsRetryableMessage() {
        val message = downloadSaveStatusMessage(DownloadSaveStatus.Failed)

        assertEquals("저장하지 못했어요. 네트워크 상태를 확인한 뒤 다시 시도해주세요.", message)
    }

    @Test
    fun idleStatusDoesNotShowMessage() {
        val message = downloadSaveStatusMessage(DownloadSaveStatus.Idle)

        assertEquals(null, message)
    }
}
