package com.example.musicplayer.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.example.musicplayer.AppController
import com.example.musicplayer.AppController.Companion.channelId
import com.example.musicplayer.MainActivity.Companion.getThumbnail
import com.example.musicplayer.R
import com.example.musicplayer.`interface`.PlayerInterface
import com.example.musicplayer.broadcast.NotificationReceiver
import com.example.musicplayer.services.MusicService
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity(), PlayerInterface, ServiceConnection {

    private val TAG = "PlayerActivity"

    //    var mediaPlayer: MediaPlayer? = null
    lateinit var musicService: MusicService

    var isPlaying = false
    var isShuffle = false
    var isRepeat = false

    lateinit var mediaSessionCompat: MediaSessionCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        mediaSessionCompat = MediaSessionCompat(baseContext, "My Service")
        bindService()
        clicks()
    }

    private fun bindService() {
        var intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
    }

    private fun initalize() {
        blurImage()
        setTitleAndArtistName()
        playMusic()
        setSeekBar()
        showNotification(R.drawable.ic_pause)
    }


    private fun clicks() {
        fast_rewind_iv.setOnClickListener {
            previousSong()
        }
        fast_forward_iv.setOnClickListener {
            nextSong()
        }
        back_button_iv.setOnClickListener {
            onBackPressed()
        }
        shuffle_iv.setOnClickListener {
            isShuffle = !isShuffle
            setShuffle()
        }
        repeat_iv.setOnClickListener {
            isRepeat = !isRepeat
            setRepeat()
        }
        music_play_pause_iv.setOnClickListener {
            musicPlayPause()
        }
    }

    private fun setRepeat() {
        if (isRepeat) {
            repeat_iv.setColorFilter(resources.getColor(R.color.bay_of_many), PorterDuff.Mode.SRC_IN);
        } else {
            repeat_iv.setColorFilter(resources.getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        }
    }

    private fun setShuffle() {
        if (isShuffle) {
            shuffle_iv.setColorFilter(resources.getColor(R.color.bay_of_many), PorterDuff.Mode.SRC_IN);
        } else {
            shuffle_iv.setColorFilter(resources.getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        }
    }

    private fun shuffleMusic() {
        AppController.setRandomNumber()
        initalize()
    }

    private fun repeatSong() {
        initalize()
    }

    override fun nextSong() {
        var position = AppController.currentListIndex
        position++
        if (AppController.musicList.size > position) {
            AppController.currentListIndex++
            initalize()
        } else {
            Toast.makeText(this, resources.getString(R.string.last_song), Toast.LENGTH_SHORT).show()
        }
    }

    override fun previousSong() {
        var position = AppController.currentListIndex
        position--
        if (0 <= position) {
            AppController.currentListIndex--
            initalize()
        } else {
            Toast.makeText(this, resources.getString(R.string.first_song), Toast.LENGTH_SHORT).show()
        }
    }

    override fun musicPlayPause() {
        isPlaying = !isPlaying
        if (isPlaying)
            resumeMusic()
        else
            pauseMusic()
    }

    private fun blurImage() {
        var bitmapImage = AppController.musicList.get(AppController.currentListIndex).thumbnail
        if (bitmapImage != null) {
            imageAnimation(this, music_poster, bitmapImage!!)
            blur_image.setImageBitmap(bitmapImage)
        } else {
            music_poster.setImageResource(R.drawable.ic_launcher_music)
        }
    }

    private fun setTitleAndArtistName() {
        artist_name.text = AppController.musicList.get(AppController.currentListIndex).artist
        song_title.text = AppController.musicList.get(AppController.currentListIndex).title

        artist_name.isSelected = true
        song_title.isSelected = true
    }

    private fun playMusic() {
        isPlaying = true
        music_play_pause_iv.setImageDrawable(resources.getDrawable(R.drawable.ic_pause))
        var path = Uri.parse(AppController.musicList.get(AppController.currentListIndex).data)
        if (musicService.mediaPlayer != null) {
            musicService.mediaPlayer?.stop()
            musicService.mediaPlayer?.release()
            musicService.mediaPlayer = null;
            musicService.mediaPlayer = MediaPlayer.create(this, path)
            musicService.mediaPlayer?.start()
        } else {
            musicService.mediaPlayer = MediaPlayer.create(this, path)
            musicService.mediaPlayer?.start()
        }
        musicService.mediaPlayer?.setOnCompletionListener {
            if (isRepeat) {
                repeatSong()
                return@setOnCompletionListener
            }
            if (isShuffle) {
                shuffleMusic()
                return@setOnCompletionListener
            }
            nextSong()
        }
    }


    private fun setSeekBar() {
        seekbar.progressDrawable.setColorFilter(Color.parseColor("#1e3c7c"), PorterDuff.Mode.MULTIPLY)
        seekbar.thumb.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP)
        seekbar.max = musicService.mediaPlayer?.duration!!
        total_duration.text = convertSecondsToSsMm(musicService.mediaPlayer?.duration!!)

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (musicService.mediaPlayer != null && fromUser) {
                    musicService.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        GlobalScope.launch(Dispatchers.Main) {
            updateSeekBar()
        }
    }

    suspend fun updateSeekBar() {
        while (musicService.mediaPlayer != null) {
            delay(1000L)
            seekbar.setProgress(musicService.mediaPlayer?.currentPosition!!)
            current_duration.text = convertSecondsToSsMm(musicService.mediaPlayer?.currentPosition!!)
        }
    }

    fun convertSecondsToSsMm(seconds: Int): String? {
        val s = (seconds / 1000) % 60
        val m = (seconds / 1000) / 60
        return String.format("%02d:%02d", m, s)
    }

    private fun resumeMusic() {
        isPlaying = true
        music_play_pause_iv.setImageDrawable(resources.getDrawable(R.drawable.ic_pause))
        if (musicService.mediaPlayer != null) {
            musicService.mediaPlayer?.start()
            showNotification(R.drawable.ic_pause)
        }
    }

    private fun pauseMusic() {
        isPlaying = false
        music_play_pause_iv.setImageDrawable(resources.getDrawable(R.drawable.ic_play))
        if (musicService.mediaPlayer != null) {
            musicService.mediaPlayer?.pause()
            showNotification(R.drawable.ic_play)
        }
    }

    private fun stopMusic() {
        if (musicService.mediaPlayer != null) {
            musicService.mediaPlayer?.stop();
            musicService.mediaPlayer?.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMusic()
        unbindService(this)
    }

    fun imageAnimation(context: Context, imageView: ImageView, bitmap: Bitmap) {

        var animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
        var animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)

        animOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                Glide.with(context).load(bitmap).into(imageView)
                animIn.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {

                    }

                    override fun onAnimationEnd(animation: Animation?) {

                    }

                    override fun onAnimationRepeat(animation: Animation?) {

                    }

                })
                imageView.startAnimation(animIn)
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

        })

        imageView.startAnimation(animOut)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel = NotificationChannel(channelId, AppController.channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "this channel is used to play music in background"

            var notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun showNotification(playPauseImage: Int) {

        createNotificationChannel()

        var intent = Intent(this, PlayerActivity::class.java)
        var contentIntent = PendingIntent.getActivity(this, 0, intent, 0)

        var prevIntent = Intent(this, NotificationReceiver::class.java).setAction(AppController.ACTION_PREVIOUS)
        var prevPending = PendingIntent.getBroadcast(this, 0, prevIntent, 0)

        var playPauseIntent = Intent(this, NotificationReceiver::class.java).setAction(AppController.ACTION_PLAY_PAUSE)
        var playPausePending = PendingIntent.getBroadcast(this, 0, playPauseIntent, 0)

        var nextIntent = Intent(this, NotificationReceiver::class.java).setAction(AppController.ACTION_NEXT)
        var nextPending = PendingIntent.getBroadcast(this, 0, nextIntent, 0)

        var picture = getThumbnail(AppController.musicList.get(AppController.currentListIndex).data!!)
        if (picture == null)
            picture = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_music)

        var notificaiton = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(playPauseImage)
            .setLargeIcon(picture)
            .setContentTitle(AppController.musicList.get(AppController.currentListIndex).title!!)
            .setContentText(AppController.musicList.get(AppController.currentListIndex).artist!!)
            .addAction(R.drawable.ic_rewind, "rewind", prevPending)
            .addAction(playPauseImage, "play pause Intent", playPausePending)
            .addAction(R.drawable.ic_fast_forward, "fast forward", nextPending)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .build()

        var notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificaiton)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        var binder: MusicService.MyBinder = service as MusicService.MyBinder
        musicService = binder.getService()
        initalize()
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }
}