package com.example.musicplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.musicplayer.models.MusicFile

class AppController : Application() {

    private var channelId = "com.monik.musicplayer"
    private var channelName = "Music Player Channel"

    companion object {
        lateinit var context : Context
        var musicList = ArrayList<MusicFile>()
        var currentListIndex = -1

        fun setRandomNumber(){
            val count = musicList.size
            val randomNumber = (0..count).random()
            currentListIndex = randomNumber
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        createNotification()
    }

    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel = NotificationChannel(channelId , channelName , NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "this channel is used to play music in background"

            var notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}