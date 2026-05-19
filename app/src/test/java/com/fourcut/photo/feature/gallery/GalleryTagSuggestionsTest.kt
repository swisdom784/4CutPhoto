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
}
