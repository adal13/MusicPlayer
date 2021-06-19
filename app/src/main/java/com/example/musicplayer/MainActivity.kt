package com.example.musicplayer

import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.musicplayer.fragmnets.AlbumFragment
import com.example.musicplayer.fragmnets.FavouriteFragment
import com.example.musicplayer.fragmnets.HomeFragment
import com.example.musicplayer.models.MusicFile
import kotlinx.android.synthetic.main.activity_main.*
import nl.joery.animatedbottombar.AnimatedBottomBar

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private val REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (checkForPermission()) {
            init()
        } else {
            askForPermission()
        }
    }

    private fun init() {
        getAllMusicFiles()
        initTabs()
    }

    private fun initTabs() {

        var fragmentList = listOf(HomeFragment() , AlbumFragment() , FavouriteFragment())
        supportFragmentManager.beginTransaction().replace(R.id.framelayout , fragmentList[1]).commit()
        bottom_bar.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                supportFragmentManager.beginTransaction().replace(R.id.framelayout , fragmentList[newIndex]).commit()
            }

            override fun onTabReselected(index: Int, tab: AnimatedBottomBar.Tab) {

            }
        })
    }

    private fun askForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (checkForPermission()) {
                init()
            } else {
                Toast.makeText(this, resources.getString(R.string.allow_permission), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkForPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun getAllMusicFiles() {
        var musicList = mutableListOf<MusicFile>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projections = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
        )
        var cursor = contentResolver.query(uri, projections, null, null, null)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                var title = cursor.getString(0)
                var album = cursor.getString(1)
                var duration = cursor.getString(2)
                var artist = cursor.getString(3)
                var data = cursor.getString(4)

                Log.e(TAG, "getAllMusicFiles: $data")

                var musicFile = MusicFile(title, album, duration, artist, data, getThumbnail(data))
                musicList.add(musicFile)
            }
            AppController.musicList = musicList
        } else {
            println("cursor is null")
        }
    }


    private fun getThumbnail(uri: String): ByteArray? {
        try {
            var retriever = MediaMetadataRetriever()
            retriever.setDataSource(uri)
            var art = retriever.embeddedPicture
            retriever.release()
            return art
        } catch (e: Exception) {
            return ByteArray(0)
        }
    }
}