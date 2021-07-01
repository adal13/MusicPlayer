package com.builders.musicplayer.fragmnets

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.builders.musicplayer.AppController
import com.builders.musicplayer.R
import com.builders.musicplayer.adapters.MusicAdapter
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private  val TAG = "HomeFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onResume() {
        super.onResume()
        AppController.setMusicListForHome()
        initList()
    }

    private fun initList() {
        var musicAdapter = MusicAdapter(AppController.musicList, context)
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = musicAdapter
    }
}