package io.github.isaquefontinele.lido.capture

import io.github.isaquefontinele.lido.db.LidoDatabase
import io.github.isaquefontinele.lido.library.LibraryRepository
import io.github.isaquefontinele.lido.library.createTestDriver
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArticlePipelineTest {
    private val driver = createTestDriver()
    private val repository = LibraryRepository(LidoDatabase(driver), StandardTestDispatcher())

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    private fun pipeline(fetcher: PageFetcher) =
        ArticlePipeline(repository, fetcher, ArticleExtractor(), now = { 1_000 })

    @Test
    fun storesExtractedContentAndMetadata() = runTest {
        val id = repository.save("1", "https://example.com/offline", "Shared title", savedAt = 1)
        val fetcher = PageFetcher(mockClient { respondHtml(SAMPLE_ARTICLE_HTML) })

        assertEquals(PipelineResult.Extracted, pipeline(fetcher).process(id))

        val item = assertNotNull(repository.item(id))
        assertEquals("Why Offline Reading Matters", item.title)
        assertEquals("Ana Souza", item.author)
        assertEquals("Example Blog", item.siteName)
        assertEquals("https://example.com/images/lead.jpg", item.leadImageUrl)
        assertEquals(1_000, item.extractedAt)
        val html = assertNotNull(repository.content(id))
        assertTrue("Long-form reading deserves a quiet place" in html)
        assertTrue("About" !in html, "navigation should be removed")
    }

    @Test
    fun fetchFailuresAreReportedAndStoreNothing() = runTest {
        val id = repository.save("1", "https://example.com/down", "Shared title", savedAt = 1)
        val fetcher = PageFetcher(mockClient { respondError(HttpStatusCode.BadGateway) })

        assertEquals(PipelineResult.Failed("HTTP 502", retryable = true), pipeline(fetcher).process(id))

        val item = assertNotNull(repository.item(id))
        assertEquals("Shared title", item.title)
        assertNull(item.extractedAt)
        assertNull(repository.content(id))
    }

    @Test
    fun unknownItemIsNotRetried() = runTest {
        val fetcher = PageFetcher(mockClient { respondHtml(SAMPLE_ARTICLE_HTML) })

        assertEquals(false, (pipeline(fetcher).process("missing") as PipelineResult.Failed).retryable)
    }
}
