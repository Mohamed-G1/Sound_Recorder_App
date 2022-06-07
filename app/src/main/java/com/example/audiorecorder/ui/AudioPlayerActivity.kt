package com.example.audiorecorder.ui

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.audiorecorder.R
import com.example.audiorecorder.databinding.ActivityAudioPlayerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import javax.annotation.Resource

class AudioPlayerActivity : AppCompatActivity() {
    lateinit var binding: ActivityAudioPlayerBinding
    lateinit var mediaPlayer: MediaPlayer

    lateinit var runnable: Runnable
    lateinit var handler: Handler
    var jumpValue = 1000
    var chipPlaySpeed = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_audio_player)

        setUpActionBar()


        // put audio and string name from another screen
        var filePath = intent.getStringExtra("filepath")
        var fileName = intent.getStringExtra("filename")
        binding.tvFileName.text = fileName

        //play file path with media player


        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setDataSource(filePath)
            prepare()
            start()

            var audio = mediaPlayer.audioSessionId
            if (audio != -1) {
                binding.visualizer.setAudioSessionId(audio)
            }



        }




        handleTrackDurationTV()

        // this to seek bar konw that will start from 0 and end the player duration
        binding.seekBar.max = mediaPlayer.duration


        // to move seek bar with player in background thread
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            binding.seekBar.progress = mediaPlayer.currentPosition
            handleTrackProgressTV()
            handler.postDelayed(runnable, 1000L)
        }




        GlobalScope.launch(Dispatchers.Main) {

        }



        binding.playButton.setOnClickListener {
            lifecycleScope.launch {
                playPausePlayer()
            }
        }

        // this to if player complete the pause button returned into play button
        mediaPlayer.setOnCompletionListener {
            binding.playButton.background =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, theme)
            // to remove from main loop thread
            handler.removeCallbacks(runnable)
        }

        // handle forward
        binding.playForward.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition + jumpValue)
            binding.seekBar.progress += jumpValue
        }

        // handle backward
        binding.playBackward.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition - jumpValue)
            binding.seekBar.progress -= jumpValue
        }

        // handle chip button
        binding.chip.setOnClickListener {
            if (chipPlaySpeed != 2f)
                chipPlaySpeed += 0.5f
            else
                chipPlaySpeed = 0.5f
            mediaPlayer.playbackParams = PlaybackParams().setSpeed(chipPlaySpeed)
            binding.chip.text = "x $chipPlaySpeed"
        }
        movingSeekBar()

        playPausePlayer()

    }

    fun initializeUIMediaPlayer() {


    }

    // this to setup action bar
    fun setUpActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    // to handle moving seekbar with player
    private fun movingSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, position: Int, moving: Boolean) {
                if (moving) {
                    mediaPlayer.seekTo(position)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private  fun playPausePlayer() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            binding.playButton.background =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_pause_circle, theme)
            binding.visualizer.setAudioSessionId(mediaPlayer.audioSessionId)
            handler.postDelayed(runnable, 1000L)

        } else {
            mediaPlayer.pause()
            binding.playButton.background =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, theme)
            // to remove from main loop thread
            handler.removeCallbacks(runnable)
        }

    }

    // to handle when back pressed stop player and remove thread
    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer.stop()
        mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }

    // this fun to convert int time format to string to show it in tv
    fun dateFormat(duration: Int): String {
        var d = duration / 1000
        var s = d % 60
        var m = (d / 60 % 60)
        var h = ((d - m * 60) / 360).toInt()

        val f: NumberFormat = DecimalFormat("00")
        var str = "$m:${f.format(s)}"

        if (h > 0) {
            str = "$h:$str"
        }
        return str
    }

    // this to show time of track player
    fun handleTrackDurationTV() {
        binding.tvTrackDuration.text = dateFormat(mediaPlayer.duration)
    }

    // this will play with handler we created bez change value with seekbar
    fun handleTrackProgressTV() {
        binding.tvTrackProgress.text = dateFormat((mediaPlayer.currentPosition))
    }
}