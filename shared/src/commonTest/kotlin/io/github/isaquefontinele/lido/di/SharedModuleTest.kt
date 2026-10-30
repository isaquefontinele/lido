package io.github.isaquefontinele.lido.di

import app.cash.sqldelight.db.SqlDriver
import io.github.isaquefontinele.lido.library.LibraryRepository
import io.github.isaquefontinele.lido.library.createTestDriver
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class SharedModuleTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun sharedModuleResolvesRepository() {
        val testPlatform = module { single<SqlDriver> { createTestDriver() } }
        val koin = koinApplication { modules(testPlatform, sharedModule) }.koin

        assertNotNull(koin.get<LibraryRepository>())
    }
}
