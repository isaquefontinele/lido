package io.github.isaquefontinele.lido.di

import io.github.isaquefontinele.lido.capture.CaptureService
import io.github.isaquefontinele.lido.db.LidoDatabase
import io.github.isaquefontinele.lido.library.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Platform bindings, such as the SQLDelight driver. */
expect val platformModule: Module

internal const val DATABASE_NAME = "lido.db"

val sharedModule = module {
    single { LidoDatabase(get()) }
    single { LibraryRepository(get(), Dispatchers.IO) }
    single {
        @OptIn(ExperimentalUuidApi::class)
        CaptureService(
            repository = get(),
            dispatcher = Dispatchers.IO,
            newId = { Uuid.random().toString() },
            now = { Clock.System.now().toEpochMilliseconds() },
        )
    }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(platformModule, sharedModule)
}
