package io.github.isaquefontinele.lido.library

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.github.isaquefontinele.lido.capture.Article
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

    fun item(id: String): Item? = database.itemQueries.selectById(id).executeAsOneOrNull()

    fun content(itemId: String): String? =
        database.articleContentQueries.selectByItemId(itemId).executeAsOneOrNull()

    /** Saves a URL and returns the id of its item, which is the existing one if it was already saved. */
    fun save(id: String, url: String, title: String?, savedAt: Long): String =
        database.transactionWithResult {
            database.itemQueries.insert(id, url, title, savedAt)
            database.itemQueries.selectIdByUrl(url).executeAsOne()
        }

    fun saveArticle(itemId: String, article: Article, extractedAt: Long) {
        database.transaction {
            database.itemQueries.updateExtractedMetadata(
                title = article.title,
                author = article.author,
                siteName = article.siteName,
                leadImageUrl = article.leadImageUrl,
                excerpt = article.excerpt,
                extractedAt = extractedAt,
                id = itemId,
            )
            database.articleContentQueries.upsert(itemId, article.html)
        }
    }

    fun delete(id: String) {
        database.transaction {
            database.articleContentQueries.deleteByItemId(id)
            database.itemQueries.deleteById(id)
        }
    }
}
