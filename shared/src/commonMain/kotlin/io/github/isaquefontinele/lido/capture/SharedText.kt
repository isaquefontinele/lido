package io.github.isaquefontinele.lido.capture

private val urlRegex = Regex("""https?://[^\s<>"]+""", RegexOption.IGNORE_CASE)

/** Punctuation that usually ends the surrounding sentence rather than the URL. */
private const val TRAILING_PUNCTUATION = ".,;:!?'\"”’…"

/**
 * Returns the first http(s) URL in text shared from another app, or null if there is none.
 *
 * Apps often share a title or comment along with the link ("Great read: https://… via @app"),
 * so the URL is searched for instead of taking the whole text.
 */
fun extractUrl(sharedText: String): String? {
    val match = urlRegex.find(sharedText) ?: return null
    return trimTrailing(match.value).takeIf { it.length > match.value.indexOf("://") + 3 }
}

private fun trimTrailing(url: String): String {
    var end = url.length
    while (end > 0) {
        val c = url[end - 1]
        end = when {
            c in TRAILING_PUNCTUATION -> end - 1
            // Drop a closing bracket only when it has no opening partner inside the URL,
            // so Wikipedia-style links like .../Kotlin_(language) stay intact.
            c == ')' && url.take(end).count { it == '(' } < url.take(end).count { it == ')' } -> end - 1
            c == ']' && url.take(end).count { it == '[' } < url.take(end).count { it == ']' } -> end - 1
            else -> return url.take(end)
        }
    }
    return ""
}
