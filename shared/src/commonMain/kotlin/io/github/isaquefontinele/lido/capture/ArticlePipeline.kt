package io.github.isaquefontinele.lido.capture

import io.github.isaquefontinele.lido.library.LibraryRepository
import kotlinx.coroutines.CancellationException

sealed interface PipelineResult {
    data object Extracted : PipelineResult
    data class Failed(val reason: String, val retryable: Boolean) : PipelineResult
}

/** Fetches a saved item's page, extracts the article and stores content and metadata. */
class ArticlePipeline(
    private val repository: LibraryRepository,
    private val fetcher: PageFetcher,
    private val extractor: ArticleExtractor,
    private val now: () -> Long,
) {
    suspend fun process(itemId: String): PipelineResult {
        val item = repository.item(itemId)
            ?: return PipelineResult.Failed("Item $itemId not found", retryable = false)

        val page = when (val fetched = fetcher.fetch(item.url)) {
            is FetchResult.Success -> fetched.page
            is FetchResult.Failure -> return PipelineResult.Failed(fetched.reason, fetched.retryable)
        }
        val article = try {
            extractor.extract(page)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return PipelineResult.Failed("Extraction failed: ${e.message}", retryable = false)
        }
        repository.saveArticle(itemId, article, extractedAt = now())
        return PipelineResult.Extracted
    }
}
