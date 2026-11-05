package io.github.isaquefontinele.lido.capture

import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class PageFetcherTest {
    @Test
    fun returnsTheHtmlOfAPage() = runTest {
        val fetcher = PageFetcher(mockClient { respondHtml("<p>hi</p>") })

        assertEquals(
            FetchResult.Success(FetchedPage("https://example.com/a", "<p>hi</p>")),
            fetcher.fetch("https://example.com/a"),
        )
    }

    @Test
    fun serverErrorsAndRateLimitsAreRetryable() = runTest {
        for (status in listOf(HttpStatusCode.ServiceUnavailable, HttpStatusCode.TooManyRequests)) {
            val fetcher = PageFetcher(mockClient { respondError(status) })

            assertEquals(
                FetchResult.Failure("HTTP ${status.value}", retryable = true),
                fetcher.fetch("https://example.com/a"),
            )
        }
    }

    @Test
    fun clientErrorsAreNotRetryable() = runTest {
        val fetcher = PageFetcher(mockClient { respondError(HttpStatusCode.NotFound) })

        assertEquals(FetchResult.Failure("HTTP 404", retryable = false), fetcher.fetch("https://example.com/a"))
    }

    @Test
    fun nonHtmlContentIsRejected() = runTest {
        val fetcher = PageFetcher(
            mockClient {
                respond("%PDF", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/pdf"))
            },
        )

        val result = fetcher.fetch("https://example.com/file.pdf")

        assertEquals(false, (result as FetchResult.Failure).retryable)
    }

    @Test
    fun networkErrorsAreRetryable() = runTest {
        val fetcher = PageFetcher(mockClient { throw IOException("No route to host") })

        assertEquals(
            FetchResult.Failure("No route to host", retryable = true),
            fetcher.fetch("https://example.com/a"),
        )
    }
}
