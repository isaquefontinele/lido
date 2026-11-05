package io.github.isaquefontinele.lido.capture

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

fun mockClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
    HttpClient(MockEngine(handler))

fun MockRequestHandleScope.respondHtml(html: String, status: HttpStatusCode = HttpStatusCode.OK) =
    respond(html, status, headersOf(HttpHeaders.ContentType, "text/html; charset=utf-8"))

val SAMPLE_ARTICLE_HTML = """
    <!doctype html>
    <html lang="en">
    <head>
      <title>Why Offline Reading Matters | Example Blog</title>
      <meta property="og:title" content="Why Offline Reading Matters">
      <meta property="og:site_name" content="Example Blog">
      <meta property="og:image" content="https://example.com/images/lead.jpg">
      <meta name="author" content="Ana Souza">
      <meta name="description" content="A short essay about reading without a connection.">
    </head>
    <body>
      <nav><a href="/">Home</a> <a href="/about">About</a></nav>
      <article>
        <h1>Why Offline Reading Matters</h1>
        <p>Long-form reading deserves a quiet place. On the subway, on a plane or in a cabin
        far from any tower, the article you saved should simply be there, without spinners.</p>
        <p>Saving the text and images locally means the reading experience no longer depends
        on the network, on trackers or on pop-ups asking for consent before every paragraph.</p>
        <p>It also means your library outlives any single company deciding to shut down.</p>
      </article>
      <footer>© Example Blog</footer>
    </body>
    </html>
""".trimIndent()
