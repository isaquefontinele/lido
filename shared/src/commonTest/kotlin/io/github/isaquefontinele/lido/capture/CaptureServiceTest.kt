package io.github.isaquefontinele.lido.capture

import io.github.isaquefontinele.lido.db.LidoDatabase
import io.github.isaquefontinele.lido.library.LibraryRepository
import io.github.isaquefontinele.lido.library.createTestDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CaptureServiceTest {
    private val driver = createTestDriver()
    private val dispatcher = StandardTestDispatcher()
    private val repository = LibraryRepository(LidoDatabase(driver), dispatcher)
    private var nextId = 0
    private val service = CaptureService(repository, dispatcher, newId = { "id${nextId++}" }, now = { 42 })

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    @Test
    fun savesTheUrlWithTheSubjectAsTitle() = runTest(dispatcher) {
        val result = service.saveSharedText("Look https://example.com/a", subject = "An article")

        assertEquals(CaptureResult.Saved("https://example.com/a"), result)
        val item = repository.observeItems().first().single()
        assertEquals("https://example.com/a", item.url)
        assertEquals("An article", item.title)
        assertEquals(42, item.savedAt)
    }

    @Test
    fun subjectThatIsJustALinkIsNotUsedAsTitle() = runTest(dispatcher) {
        service.saveSharedText("https://example.com/a", subject = "https://example.com/a")

        assertNull(repository.observeItems().first().single().title)
    }

    @Test
    fun textWithoutUrlSavesNothing() = runTest(dispatcher) {
        assertEquals(CaptureResult.NoUrl, service.saveSharedText("no link here"))
        assertEquals(emptyList(), repository.observeItems().first())
    }
}
