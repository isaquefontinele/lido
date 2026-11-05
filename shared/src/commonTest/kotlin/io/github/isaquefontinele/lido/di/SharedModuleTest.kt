package io.github.isaquefontinele.lido.di

import app.cash.sqldelight.db.SqlDriver
import io.github.isaquefontinele.lido.capture.ArticlePipeline
import io.github.isaquefontinele.lido.capture.ExtractionScheduler
import io.github.isaquefontinele.lido.capture.FetchQueueProcessor
import io.github.isaquefontinele.lido.capture.QueueRunTrigger
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
    fun sharedModuleResolvesItsServices() {
        val testPlatform = module {
            single<SqlDriver> { createTestDriver() }
            single<QueueRunTrigger> { QueueRunTrigger {} }
        }
        val koin = koinApplication { modules(testPlatform, sharedModule) }.koin

        assertNotNull(koin.get<LibraryRepository>())
        assertNotNull(koin.get<ArticlePipeline>())
        assertNotNull(koin.get<ExtractionScheduler>())
        assertNotNull(koin.get<FetchQueueProcessor>())
    }
}
