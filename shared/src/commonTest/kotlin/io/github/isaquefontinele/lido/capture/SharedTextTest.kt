package io.github.isaquefontinele.lido.capture

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SharedTextTest {
    @Test
    fun plainUrl() {
        assertEquals("https://example.com/post", extractUrl("https://example.com/post"))
    }

    @Test
    fun urlSurroundedByTitleAndComment() {
        assertEquals(
            "https://example.com/a?b=1&c=2#top",
            extractUrl("Great read: https://example.com/a?b=1&c=2#top via @someapp"),
        )
    }

    @Test
    fun firstUrlWins() {
        assertEquals("http://one.example", extractUrl("http://one.example and https://two.example"))
    }

    @Test
    fun urlOnItsOwnLine() {
        assertEquals("https://example.com/x", extractUrl("Some title\nhttps://example.com/x\n"))
    }

    @Test
    fun trailingSentencePunctuationIsDropped() {
        assertEquals("https://example.com/x", extractUrl("Read this: https://example.com/x."))
        assertEquals("https://example.com/x", extractUrl("\"https://example.com/x\","))
    }

    @Test
    fun unbalancedClosingParenthesisIsDropped() {
        assertEquals("https://example.com/x", extractUrl("(see https://example.com/x)"))
    }

    @Test
    fun balancedParenthesesAreKept() {
        assertEquals(
            "https://en.wikipedia.org/wiki/Kotlin_(programming_language)",
            extractUrl("https://en.wikipedia.org/wiki/Kotlin_(programming_language)"),
        )
    }

    @Test
    fun schemeIsCaseInsensitive() {
        assertEquals("HTTPS://Example.com", extractUrl("HTTPS://Example.com"))
    }

    @Test
    fun textWithoutUrl() {
        assertNull(extractUrl("just some words"))
        assertNull(extractUrl("ftp://example.com/file"))
        assertNull(extractUrl("https://"))
        assertNull(extractUrl(""))
    }
}
