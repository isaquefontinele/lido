package io.github.isaquefontinele.lido.capture

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Extraction checks against synthetic pages saved under resources/fixtures (no live sites). */
class ArticleExtractorFixtureTest {
    private val extractor = ArticleExtractor()

    private fun fixture(name: String, url: String): FetchedPage {
        val html = checkNotNull(javaClass.getResource("/fixtures/$name")) { "Missing fixture $name" }.readText()
        return FetchedPage(url, html)
    }

    @Test
    fun newsArticle() = runTest {
        val article = extractor.extract(
            fixture("news-article.html", "https://jornal.example.com/cidades/ciclovias-zona-norte"),
        )

        assertEquals("Prefeitura amplia ciclovias na zona norte", article.title)
        assertEquals("Carla Mendes", article.author)
        assertEquals("Jornal Exemplo", article.siteName)
        assertEquals("https://jornal.example.com/media/2026/ciclovia.jpg", article.leadImageUrl)
        assertEquals("Novos 40 km de ciclovias devem ficar prontos até março.", article.excerpt)
        assertTrue("40 quilômetros de novas ciclovias" in article.html)
        assertTrue("bicicletários" in article.html)
        for (clutter in listOf("PUBLICIDADE", "Leia também", "newsletter", "Todos os direitos", "<script")) {
            assertTrue(clutter !in article.html, "'$clutter' should be removed")
        }
    }

    @Test
    fun blogPostKeepsHeadingsAndCode() = runTest {
        val article = extractor.extract(fixture("blog-post.html", "https://sam.example.org/small-commits"))

        assertEquals("Notes on Writing Small Commits", article.title)
        assertEquals("Sam Rivera", article.author)
        assertEquals("https://cdn.example.org/covers/commits.png", article.leadImageUrl)
        assertTrue("Keep the build green" in article.html)
        assertTrue("git add -p" in article.html)
        assertTrue("Archive" !in article.html, "sidebar should be removed")
        assertTrue("Great post!" !in article.html, "comments should be removed")
    }
}
