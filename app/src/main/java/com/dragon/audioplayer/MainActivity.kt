package com.dragon.audioplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    lateinit var player: AudioPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        player = AudioPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}