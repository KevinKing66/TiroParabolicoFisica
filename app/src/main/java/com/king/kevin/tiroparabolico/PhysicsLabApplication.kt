package com.king.kevin.tiroparabolico

import android.app.Application
import com.king.kevin.tiroparabolico.di.AppContainer

class PhysicsLabApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
