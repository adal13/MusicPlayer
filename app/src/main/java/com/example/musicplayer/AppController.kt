package com.example.musicplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.musicplayer.models.MusicFile

class AppController : Application() {



    companion object {

        var channelId = "com.monik.musicplayer"
        var channelName = "Music Player Channel"

        var ACTION_PREVIOUS = "ACTION_PREVIOUS"
        var ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
        var ACTION_NEXT = "ACTION_NEXT"

        lateinit var context: Context
        var musicList = ArrayList<MusicFile>()
        var currentListIndex = -1

        fun setRandomNumber() {
            val count = musicList.size
            val randomNumber = (0..count).random()
            currentListIndex = randomNumber
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}