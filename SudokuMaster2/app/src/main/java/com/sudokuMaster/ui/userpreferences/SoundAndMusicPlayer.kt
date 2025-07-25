package com.sudokuMaster.ui.userpreferences

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface

class SoundAndMusicPlayer(private val context: Context) {
    private var soundEffectPlayer: MediaPlayer? = null // Per gli effetti sonori brevi
    private var musicPlayer: MediaPlayer? = null       // Per la musica di sottofondo

    @RawRes
    private var currentBackgroundMusicResId: Int? = null // Tiene traccia dell'ID della musica di sottofondo corrente
    private var wasMusicPlayingBeforePause = false // Indica se la musica stava suonando prima di essere messa in pausa
    private val TAG = "MusicPlayer"

    fun playSoundEffect(@RawRes soundResId: Int) {
        soundEffectPlayer?.release()
        soundEffectPlayer = null

        // Crea e avvia un nuovo MediaPlayer per l'effetto sonoro
        MediaPlayer.create(context, soundResId)?.apply {
            setOnCompletionListener { mp -> mp.release() } // Rilascia il player quando il suono finisce
            start()
            soundEffectPlayer = this // Salva il riferimento per poterlo rilasciare se necessario
        }
    }

    fun playBackgroundMusic(@RawRes musicResId: Int) {
        // Caso 1: La stessa musica è già in riproduzione attiva. Non fare nulla.
        if (musicPlayer != null && musicPlayer?.isPlaying == true && currentBackgroundMusicResId == musicResId) {
            return
        }

        // Caso 2: La stessa musica è stata messa in pausa. Riprendila.
        if (musicPlayer != null && currentBackgroundMusicResId == musicResId && !musicPlayer!!.isPlaying) {
            try {
                musicPlayer?.start()
                wasMusicPlayingBeforePause = true // Marca che ora sta suonando
            } catch (e: Exception) {
                // Cattura eccezioni se il player è in uno stato invalido (es. rilasciato inaspettatamente)
                e.printStackTrace()
                // Forza la ricreazione in caso di errore inatteso
                stopBackgroundMusic()
                createAndStartMusicPlayer(musicResId)
            }
            return
        }

        // Caso 3: Musica diversa o nessun player esistente. Crea un nuovo player.
        stopBackgroundMusic()
        createAndStartMusicPlayer(musicResId)
    }

    private fun createAndStartMusicPlayer(@RawRes musicResId: Int) {
        musicPlayer = MediaPlayer.create(context, musicResId).apply {
            isLooping = true // Imposta la musica per il loop
            setVolume(0.5f, 0.5f) // Imposta un volume iniziale
            start()
        }
        currentBackgroundMusicResId = musicResId
        wasMusicPlayingBeforePause = true // La musica sta suonando attivamente
    }


    fun pauseBackgroundMusic() {
        if (musicPlayer?.isPlaying == true) {
            musicPlayer?.pause()
            wasMusicPlayingBeforePause = true // La musica era in riproduzione prima della pausa
        } else {
            wasMusicPlayingBeforePause = false // Se non stava suonando, non c'è nulla da riprendere
        }
    }

    fun resumeBackgroundMusic() {
        // Riprendi solo se il player esiste, non sta già suonando, e stava suonando prima di una pausa esplicita
        if (musicPlayer != null && !musicPlayer!!.isPlaying && wasMusicPlayingBeforePause) {
            try {
                musicPlayer?.start()
            } catch (e: Exception) {
                e.printStackTrace()
                // Se c'è un errore nella ripresa (es. player in stato invalido), prova a ricreare
                currentBackgroundMusicResId?.let { playBackgroundMusic(it) }
            }
        }
    }

    fun stopBackgroundMusic() {
        musicPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        musicPlayer = null
        currentBackgroundMusicResId = null
        wasMusicPlayingBeforePause = false // Reset dello stato
    }

    // Chiamato quando l'Activity viene distrutta per rilasciare tutte le risorse
    fun release() {
        stopBackgroundMusic() // Ferma e rilascia il music player
        soundEffectPlayer?.release() // Rilascia anche l'ultimo sound effect player
        soundEffectPlayer = null
    }

    private lateinit var userPreferencesRepository: UserPreferencesRepositoryInterface

    fun setPreferencesRepository(repo: UserPreferencesRepositoryInterface) {
        this.userPreferencesRepository = repo
    }
}

