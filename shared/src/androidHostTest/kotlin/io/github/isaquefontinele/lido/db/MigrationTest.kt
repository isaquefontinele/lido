package io.github.isaquefontinele.lido.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MigrationTest {
    @Test
    fun version1DatabaseKeepsItsItemsAfterMigrating() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        // Schema as shipped in version 1.
        driver.execute(
            null,
            "CREATE TABLE Item (id TEXT NOT NULL PRIMARY KEY, url TEXT NOT NULL UNIQUE, title TEXT, savedAt INTEGER NOT NULL)",
            0,
        )
        driver.execute(null, "INSERT INTO Item VALUES ('1', 'https://example.com', 'Old', 5)", 0)

        LidoDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = LidoDatabase.Schema.version)

        val database = LidoDatabase(driver)
        val item = database.itemQueries.selectById("1").executeAsOne()
        assertEquals("Old", item.title)
        assertNull(item.extractedAt)
        database.articleContentQueries.upsert("1", "<p>content</p>")
        assertEquals("<p>content</p>", database.articleContentQueries.selectByItemId("1").executeAsOne())
        driver.close()
    }
}
