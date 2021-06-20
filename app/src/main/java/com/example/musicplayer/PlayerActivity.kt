package com.example.musicplayer

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.bumptech.glide.request.target.Target
import com.example.musicplayer.custom.BlurTransformation
import kotlinx.android.synthetic.main.activity_player.*


class PlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        blurImage()
    }

    private fun blurImage() {
        var bitmapImage = AppController.musicList.get(AppController.currentListIndex).thumbnail
        blur_image.setImageBitmap(bitmapImage)
        Glide.with(this).asBitmap().load(bitmapImage).into(music_poster)
        blur_image.setBlur(2)
    }
}