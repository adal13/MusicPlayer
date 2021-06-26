package com.example.musicplayer

import android.graphics.Color
import android.graphics.PorterDuff
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class PlayerActivity : AppCompatActivity() {

    private val TAG = "PlayerActivity"
    var mediaPlayer: MediaPlayer? = null
    var isPlaying = false
    val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        initalize()
        clicks()
    }

    private fun initalize() {
        blurImage()
        setTitleAndArtistName()
        playMusic()
        setSeekBar()
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
        music_play_pause_iv.setOnClickListener {
            isPlaying = !isPlaying
            if (isPlaying)
                resumeMusic()
            else
                pauseMusic()
        }
    }

    private fun nextSong() {
        var position = AppController.currentListIndex
        position++
        if (AppController.musicList.size > position) {
            AppController.currentListIndex++
            initalize()
        }
    }

    private fun previousSong() {
        var position = AppController.currentListIndex
        position--
        if (0 <= position) {
            AppController.currentListIndex--
            initalize()
        }
    }

    private fun blurImage() {
        var bitmapImage = AppController.musicList.get(AppController.currentListIndex).thumbnail
        blur_image.setImageBitmap(bitmapImage)
        Glide.with(this).asBitmap().load(bitmapImage).into(music_poster)
        blur_image.setBlur(1)
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
        if (mediaPlayer != null) {
            Log.e(TAG, "playMusic: mediaPlayer != null")
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null;
            mediaPlayer = MediaPlayer.create(this, path)
            mediaPlayer?.start()
        } else {
            mediaPlayer = MediaPlayer.create(this, path)
            mediaPlayer?.start()
        }
        mediaPlayer?.setOnCompletionListener {
            nextSong()
        }
    }

    private fun setSeekBar() {
        seekbar.progressDrawable.setColorFilter(Color.parseColor("#1e3c7c"), PorterDuff.Mode.MULTIPLY)
        seekbar.thumb.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP)
        seekbar.max = mediaPlayer?.duration!!
        total_duration.text = convertSecondsToSsMm(mediaPlayer?.duration!!)

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer?.seekTo(progress)
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
        while (mediaPlayer != null) {
            delay(1000L)
            seekbar.setProgress(mediaPlayer?.currentPosition!!)
            current_duration.text = convertSecondsToSsMm(mediaPlayer?.currentPosition!!)

        }
    }

    fun convertSecondsToSsMm(seconds: Int): String? {
        val s = (seconds / 1000) % 60
        val m = (seconds / 1000) / 60
        return String.format("%02d:%02d",  m, s)
    }

    private fun resumeMusic() {
        isPlaying = true
        music_play_pause_iv.setImageDrawable(resources.getDrawable(R.drawable.ic_pause))
        if (mediaPlayer != null) {
            mediaPlayer?.start()
        }
    }

    private fun pauseMusic() {
        isPlaying = false
        music_play_pause_iv.setImageDrawable(resources.getDrawable(R.drawable.ic_play))
        if (mediaPlayer != null) {
            mediaPlayer?.pause()
        }
    }

    private fun stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer?.stop();
            mediaPlayer?.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMusic()
    }


}