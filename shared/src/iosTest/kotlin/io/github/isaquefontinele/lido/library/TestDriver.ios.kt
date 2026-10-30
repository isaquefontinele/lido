package io.github.isaquefontinele.lido.library

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import io.github.isaquefontinele.lido.db.LidoDatabase

actual fun createTestDriver(): SqlDriver = inMemoryDriver(LidoDatabase.Schema)
