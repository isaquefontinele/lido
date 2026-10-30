package io.github.isaquefontinele.lido.di

import io.github.isaquefontinele.lido.db.LidoDatabase
import io.github.isaquefontinele.lido.library.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/** Platform bindings, such as the SQLDelight driver. */
expect val platformModule: Module

internal const val DATABASE_NAME = "lido.db"

val sharedModule = module {
    single { LidoDatabase(get()) }
    single { LibraryRepository(get(), Dispatchers.IO) }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(platformModule, sharedModule)
}
