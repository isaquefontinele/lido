package io.github.isaquefontinele.lido.library

import io.github.isaquefontinele.lido.db.LidoDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryRepositoryTest {
    private val driver = createTestDriver()
    private val dispatcher = StandardTestDispatcher()
    private val repository = LibraryRepository(LidoDatabase(driver), dispatcher)

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    @Test
    fun savedItemsAreListedNewestFirst() = runTest(dispatcher) {
        repository.save("1", "https://example.com/a", "A", savedAt = 100)
        repository.save("2", "https://example.com/b", null, savedAt = 200)

        val items = repository.observeItems().first()

        assertEquals(listOf("2", "1"), items.map { it.id })
    }

    @Test
    fun savingTheSameUrlTwiceKeepsOneItem() = runTest(dispatcher) {
        repository.save("1", "https://example.com/a", "A", savedAt = 100)
        repository.save("2", "https://example.com/a", "A again", savedAt = 200)

        assertEquals(listOf("1"), repository.observeItems().first().map { it.id })
    }

    @Test
    fun deletedItemsAreRemoved() = runTest(dispatcher) {
        repository.save("1", "https://example.com/a", "A", savedAt = 100)
        repository.delete("1")

        assertEquals(emptyList(), repository.observeItems().first())
    }
}
