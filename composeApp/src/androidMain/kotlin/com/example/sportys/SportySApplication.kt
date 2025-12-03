package com.example.sportys

import android.app.Application
import com.example.sportys.di.initKoin
import org.koin.android.ext.koin.androidContext

class SportySApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AppContextProvider.init(this)
        initKoin { androidContext(this@SportySApplication) }
    }
}