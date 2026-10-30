package io.github.isaquefontinele.lido.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.github.isaquefontinele.lido.db.LidoDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<SqlDriver> { AndroidSqliteDriver(LidoDatabase.Schema, androidContext(), DATABASE_NAME) }
}
