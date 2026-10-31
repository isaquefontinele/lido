package io.github.isaquefontinele.lido.capture

import io.github.isaquefontinele.lido.library.LibraryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

sealed interface CaptureResult {
    data class Saved(val url: String) : CaptureResult
    data object NoUrl : CaptureResult
}

/** Saves links handed over by the platform share targets into the library. */
class CaptureService(
    private val repository: LibraryRepository,
    private val dispatcher: CoroutineDispatcher,
    private val newId: () -> String,
    private val now: () -> Long,
) {
    /**
     * Saves the first URL found in [text]. [subject] is the page title some apps (browsers in
     * particular) send alongside the link; it is kept until extraction provides a better one.
     */
    suspend fun saveSharedText(text: String, subject: String? = null): CaptureResult {
        val url = extractUrl(text) ?: return CaptureResult.NoUrl
        val title = subject?.trim()?.takeIf { it.isNotEmpty() && extractUrl(it) == null }
        withContext(dispatcher) {
            repository.save(id = newId(), url = url, title = title, savedAt = now())
        }
        return CaptureResult.Saved(url)
    }
}
