package io.github.isaquefontinele.lido

import android.app.Application
import io.github.isaquefontinele.lido.di.initKoin
import org.koin.android.ext.koin.androidContext

class LidoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@LidoApplication) }
    }
}
