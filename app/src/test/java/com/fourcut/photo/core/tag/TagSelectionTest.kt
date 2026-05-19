package com.fourcut.photo.core.tag

import org.junit.Assert.assertEquals
import org.junit.Test

class TagSelectionTest {
    @Test
    fun addTagIgnoresCaseInsensitiveDuplicates() {
        val result = addSelectedTag(listOf("Hajin"), "hajin")

        assertEquals(listOf("Hajin"), result)
    }

    @Test
    fun addTagTrimsName() {
        val result = addSelectedTag(emptyList(), "  Hajin  ")

        assertEquals(listOf("Hajin"), result)
    }

    @Test
    fun removeTagOnlyRemovesFromCurrentSelection() {
        val result = removeSelectedTag(listOf("Hajin", "JungHyun"), "hajin")

        assertEquals(listOf("JungHyun"), result)
    }
}
