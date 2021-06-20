package com.example.musicplayer

import android.app.Application
import android.content.Context
import com.example.musicplayer.models.MusicFile

class AppController : Application() {

    companion object {
        lateinit var context : Context
        var musicList = ArrayList<MusicFile>()
        var currentListIndex = -1
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}