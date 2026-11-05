package io.github.isaquefontinele.lido.di

import io.github.isaquefontinele.lido.capture.ArticleExtractor
import io.github.isaquefontinele.lido.capture.ArticlePipeline
import io.github.isaquefontinele.lido.capture.CaptureService
import io.github.isaquefontinele.lido.capture.ExtractionScheduler
import io.github.isaquefontinele.lido.capture.FetchQueueProcessor
import io.github.isaquefontinele.lido.capture.PageFetcher
import io.github.isaquefontinele.lido.capture.QueueRunTrigger
import io.github.isaquefontinele.lido.capture.QueuedExtractionScheduler
import io.github.isaquefontinele.lido.capture.RetryPolicy
import io.github.isaquefontinele.lido.db.LidoDatabase
import io.github.isaquefontinele.lido.library.LibraryRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.IO
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Platform bindings: the SQLDelight driver and the [QueueRunTrigger]. */
expect val platformModule: Module

internal const val DATABASE_NAME = "lido.db"

private const val USER_AGENT = "Mozilla/5.0 (compatible; Lido/0.1; +https://github.com/isaquefontinele/lido)"

internal fun now() = Clock.System.now().toEpochMilliseconds()

val sharedModule = module {
    single { LidoDatabase(get()) }
    single { LibraryRepository(get(), Dispatchers.IO) }
    single {
        @OptIn(ExperimentalUuidApi::class)
        CaptureService(
            repository = get(),
            dispatcher = Dispatchers.IO,
            newId = { Uuid.random().toString() },
            now = ::now,
        )
    }

    /** App-wide scope for work that must outlive the screen that started it. */
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single {
        // The engine (OkHttp on Android, Darwin on iOS) is picked from the platform dependencies.
        HttpClient {
            install(UserAgent) { agent = USER_AGENT }
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 30_000
            }
        }
    }
    single { PageFetcher(get()) }
    single { ArticleExtractor() }
    single { ArticlePipeline(get(), get(), get(), now = ::now) }
    single { RetryPolicy() }
    single { FetchQueueProcessor(get(), get(), get(), now = ::now) }
    single<ExtractionScheduler> { QueuedExtractionScheduler(get(), get(), get()) }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(platformModule, sharedModule)
}.also { app ->
    // Pick up links that were saved offline or were waiting for a retry when the app was closed.
    app.koin.get<QueueRunTrigger>().requestRun()
}
