package com.fourcut.photo.feature.gallery

fun galleryTagSuggestions(
    query: String,
    tagNames: List<String>,
    limit: Int = 6
): List<String> {
    val normalizedQuery = query.trim()
    return tagNames.asSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase() }
        .filter {
            normalizedQuery.isBlank() || it.contains(normalizedQuery, ignoreCase = true)
        }
        .take(limit)
        .toList()
}
