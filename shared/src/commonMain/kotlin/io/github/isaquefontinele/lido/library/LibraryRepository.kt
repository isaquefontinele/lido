package io.github.isaquefontinele.lido.library

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.github.isaquefontinele.lido.db.Item
import io.github.isaquefontinele.lido.db.LidoDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow

class LibraryRepository(
    private val database: LidoDatabase,
    private val dispatcher: CoroutineDispatcher,
) {
    fun observeItems(): Flow<List<Item>> =
        database.itemQueries.selectAll().asFlow().mapToList(dispatcher)

    fun save(id: String, url: String, title: String?, savedAt: Long) {
        database.itemQueries.insert(id, url, title, savedAt)
    }

    fun delete(id: String) {
        database.itemQueries.deleteById(id)
    }
}
