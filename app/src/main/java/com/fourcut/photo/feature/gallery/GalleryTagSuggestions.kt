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

fun galleryTagFilterAfterClick(
    currentQuery: String,
    tagName: String
): String {
    return if (currentQuery.trim().equals(tagName.trim(), ignoreCase = true)) {
        ""
    } else {
        tagName
    }
}

fun gallerySessionMatchesTagQuery(
    tagNames: List<String>,
    query: String
): Boolean {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) return true

    return tagNames.any { tagName ->
        tagName.contains(normalizedQuery, ignoreCase = true)
    }
}
