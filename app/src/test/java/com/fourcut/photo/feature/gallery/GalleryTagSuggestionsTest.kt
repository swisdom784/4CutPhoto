package com.fourcut.photo.feature.gallery

import org.junit.Assert.assertEquals
import org.junit.Test

class GalleryTagSuggestionsTest {
    @Test
    fun suggestionsDeduplicateTagsIgnoringCase() {
        val suggestions = galleryTagSuggestions(
            query = "",
            tagNames = listOf("Hajin", "hajin", "JungHyun")
        )

        assertEquals(listOf("Hajin", "JungHyun"), suggestions)
    }

    @Test
    fun suggestionsFilterByQueryIgnoringCase() {
        val suggestions = galleryTagSuggestions(
            query = "ha",
            tagNames = listOf("Hajin", "JungHyun", "Minji")
        )

        assertEquals(listOf("Hajin"), suggestions)
    }

    @Test
    fun suggestionsAreLimitedToSix() {
        val suggestions = galleryTagSuggestions(
            query = "",
            tagNames = listOf("A", "B", "C", "D", "E", "F", "G")
        )

        assertEquals(listOf("A", "B", "C", "D", "E", "F"), suggestions)
    }

    @Test
    fun clickingUnselectedTagAppliesFilter() {
        val nextQuery = galleryTagFilterAfterClick(
            currentQuery = "",
            tagName = "하진"
        )

        assertEquals("하진", nextQuery)
    }

    @Test
    fun clickingSelectedTagClearsFilter() {
        val nextQuery = galleryTagFilterAfterClick(
            currentQuery = "하진",
            tagName = "하진"
        )

        assertEquals("", nextQuery)
    }
}
