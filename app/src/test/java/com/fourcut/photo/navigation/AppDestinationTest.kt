package com.fourcut.photo.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationTest {
    @Test
    fun destinationLabelsAreKorean() {
        assertEquals("스캔", AppDestination.Scan.label)
        assertEquals("캘린더", AppDestination.Calendar.label)
        assertEquals("갤러리", AppDestination.Gallery.label)
    }
}
