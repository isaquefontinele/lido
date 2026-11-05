package io.github.isaquefontinele.lido.capture

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

data class FetchedPage(val url: String, val html: String)

sealed interface FetchResult {
    data class Success(val page: FetchedPage) : FetchResult

    /** [retryable] is true for failures that may go away later: no network, timeouts, 5xx, 429. */
    data class Failure(val reason: String, val retryable: Boolean) : FetchResult
}

/** Downloads the HTML of a saved page. Only the URL the user saved is contacted (NFR-02). */
class PageFetcher(private val client: HttpClient) {
    suspend fun fetch(url: String): FetchResult = try {
        val response = client.get(url) {
            header(HttpHeaders.Accept, "text/html,application/xhtml+xml;q=0.9,*/*;q=0.5")
        }
        val status = response.status
        val type = response.contentType()
        when {
            status == HttpStatusCode.TooManyRequests || status.value >= 500 ->
                FetchResult.Failure("HTTP ${status.value}", retryable = true)
            status.value >= 400 -> FetchResult.Failure("HTTP ${status.value}", retryable = false)
            type != null && !type.isHtml() ->
                FetchResult.Failure("Not an HTML page ($type)", retryable = false)
            else -> FetchResult.Success(
                FetchedPage(url = response.request.url.toString(), html = response.bodyAsText()),
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpRequestTimeoutException) {
        FetchResult.Failure("Timed out", retryable = true)
    } catch (e: IOException) {
        FetchResult.Failure(e.message ?: "Network error", retryable = true)
    } catch (e: IllegalArgumentException) {
        FetchResult.Failure(e.message ?: "Invalid URL", retryable = false)
    }

    private fun ContentType.isHtml() =
        match(ContentType.Text.Html) || match(ContentType("application", "xhtml+xml"))
}
