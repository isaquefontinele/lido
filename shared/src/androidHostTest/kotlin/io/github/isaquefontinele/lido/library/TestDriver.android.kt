package io.github.isaquefontinele.lido.library

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.github.isaquefontinele.lido.db.LidoDatabase

actual fun createTestDriver(): SqlDriver =
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { LidoDatabase.Schema.create(it) }
