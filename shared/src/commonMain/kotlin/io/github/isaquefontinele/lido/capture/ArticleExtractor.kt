package io.github.isaquefontinele.lido.capture

import com.prof18.klead.Klead
import com.prof18.klead.KleadOptions
import com.prof18.klead.KleadOutput

/** The readable part of a page plus its metadata. [html] is sanitized by Klead (NFR-04). */
data class Article(
    val title: String?,
    val author: String?,
    val siteName: String?,
    val leadImageUrl: String?,
    val excerpt: String?,
    val html: String,
)

/** Extracts the main article content from a fetched page using Klead. */
class ArticleExtractor {
    suspend fun extract(page: FetchedPage): Article {
        val result = Klead.parseHtml(
            html = page.html,
            url = page.url,
            options = KleadOptions(outputs = setOf(KleadOutput.HTML)),
        )
        val metadata = result.metadata
        return Article(
            title = metadata.title.clean(),
            author = metadata.author.clean(),
            siteName = metadata.site.clean(),
            leadImageUrl = metadata.image.clean(),
            excerpt = metadata.description.clean(),
            html = result.content.requireHtml(),
        )
    }

    private fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}
