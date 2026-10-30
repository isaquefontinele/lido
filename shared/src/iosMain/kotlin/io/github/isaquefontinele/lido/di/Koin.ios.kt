package io.github.isaquefontinele.lido.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.github.isaquefontinele.lido.db.LidoDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<SqlDriver> { NativeSqliteDriver(LidoDatabase.Schema, DATABASE_NAME) }
}

/** Entry point for Swift, which cannot call functions with default arguments. */
fun doInitKoin() {
    initKoin()
}
