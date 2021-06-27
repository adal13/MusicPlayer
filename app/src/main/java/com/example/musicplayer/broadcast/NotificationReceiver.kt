package com.example.musicplayer.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.musicplayer.services.MusicService

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        var action = intent?.action
        if (action != null) {
            var intent = Intent(context, MusicService::class.java)
            intent.action = action
            context?.startService(intent)
        }
    }
}