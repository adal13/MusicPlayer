package com.example.musicplayer.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import com.example.musicplayer.AppController

import com.example.musicplayer.models.MusicFile

class MusicService : Service() {

    private val TAG = "MusicService"

    var binder = MyBinder()
    var mediaPlayer: MediaPlayer? = MediaPlayer()
    var musicList = ArrayList<MusicFile>()
    lateinit var uri : Uri

    inner class MyBinder : Binder() {
        fun getService(): MusicService {
            return this@MusicService
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        musicList = AppController.musicList
    }

    fun createMediaPlayer(position : Int){
        mediaPlayer = MediaPlayer.create(baseContext , uri)
    }
}