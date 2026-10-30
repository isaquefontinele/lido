package io.github.isaquefontinele.lido.library

import app.cash.sqldelight.db.SqlDriver

/** A fresh in-memory database driver with the schema created. */
expect fun createTestDriver(): SqlDriver
