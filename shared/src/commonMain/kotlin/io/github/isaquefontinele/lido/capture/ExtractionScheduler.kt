package io.github.isaquefontinele.lido.capture

/** Starts fetching and extracting a saved item without blocking the share flow. */
fun interface ExtractionScheduler {
    fun schedule(itemId: String)
}
