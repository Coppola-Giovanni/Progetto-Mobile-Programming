package com.sudokuMaster.ui.userpreferences

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

class SoundPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playSound(@RawRes soundResId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, soundResId)
        mediaPlayer?.start()
    }
}