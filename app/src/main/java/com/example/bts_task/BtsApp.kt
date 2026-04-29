package com.example.bts_task

import android.app.Application
import com.example.bts_task.di.dataModule
import com.example.bts_task.di.domainModule
import com.example.bts_task.di.presentationModule
import com.yandex.mapkit.MapKitFactory
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BtsApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val apiKey = readMapkitApiKey()
        MapKitFactory.setApiKey(apiKey)
        MapKitFactory.initialize(this)

        startKoin {
            androidLogger()
            androidContext(this@BtsApp)
            modules(dataModule, domainModule, presentationModule)
        }
    }

    private fun readMapkitApiKey(): String {
        val app = packageManager.getApplicationInfo(
            packageName,
            android.content.pm.PackageManager.GET_META_DATA
        )
        return app.metaData?.getString("com.yandex.maps.mobile.api_key").orEmpty()
    }
}
