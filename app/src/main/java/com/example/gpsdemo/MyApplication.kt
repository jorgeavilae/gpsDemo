package com.example.gpsdemo

import android.app.Application
import android.location.Location

class MyApplication : Application() {
    lateinit var myLocations : List<Location>

    override fun onCreate() {
        super.onCreate()
        myLocations = emptyList()
    }

}