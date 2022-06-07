package com.example.audiorecorder.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.audiorecorder.R
import com.example.audiorecorder.databinding.ActivityMainBinding
import com.example.audiorecorder.db.AudioRecord
import com.example.audiorecorder.db.RecordsDataBase
import com.example.audiorecorder.waveAudioForm.Timer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class MainActivity
    : AppCompatActivity(), Timer.OnTimeTickListener {

    private val REQUEST_CODE = 200
    lateinit var binding: ActivityMainBinding
    lateinit var mediaRecorder: MediaRecorder
    lateinit var db: RecordsDataBase


    private var permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false

    private var dirPath = ""
    private var fileName = ""
    private var duration = ""

    var isRecording = false
    var isPausing = false

    lateinit var timer: Timer
    lateinit var amplitudes: ArrayList<Float>

    // to turn on vibrator while clicked on record
    lateinit var vibrator: Vibrator

    // bottom sheet
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
        }
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)

        // data base
        db = Room.databaseBuilder(this, RecordsDataBase::class.java, "audio_record")
            .build()


        // install bottom sheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.include.bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        lifecycleScope.launch(Dispatchers.Main) {

        }


        timer = Timer(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator



        binding.record.setOnClickListener {
            when {
                isPausing -> resumeRecording()
                isRecording -> pauseRecording()

                else -> startRecording()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }


        }

        binding.list.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }

        binding.delete.setOnClickListener {
            stopRecord()
            File("$dirPath$fileName.mp3").delete()
            Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show()

        }

        binding.done.setOnClickListener {
            stopRecord()

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.view.visibility = View.VISIBLE
            binding.include.fileNameInput.setText(fileName)
        }

        //bottom sheet buttons
        binding.include.cancel.setOnClickListener {
            File("$dirPath$fileName.mp3").delete()
            dismiss()
        }

        binding.include.save.setOnClickListener {
            dismiss()
            save()
            Toast.makeText(this, "Record saved", Toast.LENGTH_SHORT).show()

        }

        binding.view.setOnClickListener {
            File("$dirPath$fileName.mp3").delete()
            dismiss()
        }



        binding.delete.isClickable = false
        binding.view.visibility = View.GONE



        // hide keyboard
    }


    private fun save() {
        val newFileName = binding.include.fileNameInput.text.toString()

        if (newFileName != fileName) {
            var newFile = File("$dirPath$newFileName.mp3")
            File("$dirPath$fileName").renameTo(newFile)
        }

        // this to how to save to database
        var filePath = "$dirPath$newFileName.mp3"
        var timesTamp = Date().time
        var ampsPath = "$dirPath$newFileName"


        try {
            var fos = FileOutputStream(ampsPath)
            var out = ObjectOutputStream(fos)
            out.writeObject(amplitudes)
            fos.close()
            out.close()
        } catch (e: IOException) {}


        var record = AudioRecord(newFileName, filePath, timesTamp, duration, ampsPath)
        GlobalScope.launch(Dispatchers.IO) {
            db.audioRecordDao().insert(record)
        }

    }


    private fun dismiss() {
        binding.view.visibility = View.GONE
//        hideKeyBoard(binding.include.fileNameInput)

        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        }, 100)

//        delay(100L)

    }

    // to dont show keyboard while automatic
    private fun hideKeyBoard(view: View) {
        val hideKeyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        hideKeyboard.hideSoftInputFromWindow(view.windowToken, 0)

    }

    private fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder.pause()
        }
        isPausing = true
        binding.record.setImageResource(R.drawable.ic_mic)

        timer.pause()
    }

    private fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder.resume()
        }
        isPausing = false
        binding.record.setImageResource(R.drawable.ic_pause)

        timer.start()
    }

    private fun stopRecord() {
        timer.stop()

        mediaRecorder.apply {
            stop()
            release()
        }
        isPausing = false
        isRecording = false

        binding.list.visibility = View.VISIBLE
        binding.done.visibility = View.GONE

        binding.delete.isClickable = false
        binding.delete.setImageResource(R.drawable.ic_clear)

        binding.record.setImageResource(R.drawable.ic_record)

        binding.tvTimer.text = "00.00.00"



        amplitudes = binding.waves.clear()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
        }
        permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("SimpleDateFormat")
    private fun startRecording() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
        // start recording
        dirPath = "${externalCacheDir?.absoluteFile}"
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        val date: String = simpleDateFormat.format(Date())
        fileName = "audio_record_$date"

        mediaRecorder = MediaRecorder()
        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$fileName.mp3")

            try {
                prepare()
            } catch (e: IOException) {}

            start()
        }
        binding.record.setImageResource(R.drawable.ic_pause)
        isRecording = true
        isPausing = false

        timer.start()

        binding.delete.isClickable = true
        binding.delete.setImageResource(R.drawable.ic_delete)

        binding.list.visibility = View.GONE
        binding.done.visibility = View.VISIBLE
    }

    override fun onTimeTick(duration: String) {
        binding.tvTimer.text = duration
        // to just save first 2 digit in timer
        this.duration = duration.dropLast(3)

        binding.waves.addAmplitude(mediaRecorder.maxAmplitude.toFloat())
    }
}