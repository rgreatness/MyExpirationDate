package com.example.myexpirationdate

import android.app.Application

// Override the app's onCreate() function

class medApp : Application() {
    lateinit var container : AppContainer
    companion object {
        private var appInstance: medApp? = null

        fun getApp(): medApp {
            if (appInstance == null) {
                throw Exception("app is null!")
            }
            return appInstance!!
        }
    }

    override fun onCreate() {
        appInstance = this
        container = DefaultAppContainer(context = this)
        super.onCreate()
    }

}