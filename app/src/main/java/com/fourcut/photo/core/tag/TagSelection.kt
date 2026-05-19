package com.fourcut.photo.core.tag

fun addSelectedTag(
    selectedTags: List<String>,
    tagName: String
): List<String> {
    val normalized = tagName.trim()
    if (normalized.isBlank()) return selectedTags
    if (selectedTags.any { it.equals(normalized, ignoreCase = true) }) return selectedTags
    return selectedTags + normalized
}

fun removeSelectedTag(
    selectedTags: List<String>,
    tagName: String
): List<String> {
    val normalized = tagName.trim()
    return selectedTags.filterNot { it.equals(normalized, ignoreCase = true) }
}
