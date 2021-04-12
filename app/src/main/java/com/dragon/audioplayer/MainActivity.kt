package com.dragon.audioplayer

import android.animation.FloatEvaluator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation.INFINITE
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val player = AudioPlayer()
    private val animator by lazy {
        ObjectAnimator.ofObject(playingView, "rotation", FloatEvaluator(), 0f, 360f)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playingView.setOnClickListener {
            if (player.isPlaying.value == true) {
                player.stop()
            } else if (player.isPlaying.value == false) {
                player.start(checkBox.isChecked)
            }
        }
        player.isPlaying.observe(this) {
            playingView.rotation
            animator.duration = 2000
            animator.repeatCount = INFINITE
            if (it) {
                animator.start()
            } else {
                animator.cancel()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
    }
}