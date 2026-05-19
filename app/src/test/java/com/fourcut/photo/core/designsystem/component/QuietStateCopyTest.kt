package com.fourcut.photo.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Test

class QuietStateCopyTest {
    @Test
    fun galleryEmptyCopyInvitesFirstScan() {
        val copy = quietStateCopy(QuietStateKind.GalleryEmpty)

        assertEquals("Start with one QR", copy.title)
        assertEquals("Saved photo booth sessions will appear here by date, with each QR kept separate.", copy.message)
        assertEquals("Open Scan", copy.primaryActionLabel)
    }

    @Test
    fun gallerySearchCopySuggestsChangingTagSearch() {
        val copy = quietStateCopy(QuietStateKind.GallerySearchEmpty)

        assertEquals("No matching people yet", copy.title)
        assertEquals("Try another name or add that person tag while saving or editing a session.", copy.message)
        assertEquals(null, copy.primaryActionLabel)
    }

    @Test
    fun calendarDayCopyKeepsSameDaySessionContext() {
        val copy = quietStateCopy(QuietStateKind.CalendarDayEmpty)

        assertEquals("Nothing saved on this day", copy.title)
        assertEquals("When you save several QR sessions on the same date, they will stay separated here.", copy.message)
        assertEquals(null, copy.primaryActionLabel)
    }

    @Test
    fun cameraPermissionCopyShowsCameraAction() {
        val copy = quietStateCopy(QuietStateKind.CameraPermission)

        assertEquals("Camera access needed", copy.title)
        assertEquals("Allow the camera to scan photo booth QR codes as soon as the app opens.", copy.message)
        assertEquals("Allow camera", copy.primaryActionLabel)
    }
}
