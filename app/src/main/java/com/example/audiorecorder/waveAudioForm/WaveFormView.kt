package com.example.audiorecorder.waveAudioForm

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.audiofx.Visualizer
import android.media.audiofx.Visualizer.OnDataCaptureListener
import android.util.AttributeSet
import android.view.View

class WaveFormView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var paint = Paint()
    var amplitudes = ArrayList<Float>()
    var spikes = ArrayList<RectF>()
    protected var mVisualizer: Visualizer? = null
    protected lateinit var mRawAudioBytes: ByteArray
    var radios = 6f
    var w = 9f
    var d = 6f
    var sw = 0f
    var sh = 400f
    var maxSpikes = 0

    init {
        paint.color = Color.rgb(198, 15, 71)
        sw = resources.displayMetrics.widthPixels.toFloat()
        maxSpikes = (sw / (w + d)).toInt()
    }

    fun addAmplitude(amp: Float) {
        var normal = Math.min(amp.toInt() / 7, 400).toFloat()
        amplitudes.add(normal)


        spikes.clear()
        var amps = amplitudes.takeLast(maxSpikes)
        for (i in amps.indices) {
            var left: Float = sw - i * (w + d)
            var top = sh / 2 - amps[i] / 2
            var right: Float = left + w
            var bottom: Float = top + amps[i]
            spikes.add(RectF(left, top, right, bottom))
        }

        invalidate()

    }

    fun clear(): ArrayList<Float> {
        var amps = amplitudes.clone() as ArrayList<Float>
        amplitudes.clear()
        spikes.clear()
        invalidate()
        return amps
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
//        canvas?.drawRoundRect(RectF(20f,30f,20+30f,30f+60f), 6f,6f, paint)
//        canvas?.drawRoundRect(RectF(60f,60f,60+80f,60f+360f), 6f,6f, paint)
        spikes.forEach {
            canvas?.drawRoundRect(it, radios, radios, paint)
        }
    }


    fun setAudioSessionId(audioSessionId: Int) {
        if (mVisualizer != null)
            release()
        mVisualizer = Visualizer(audioSessionId)
        mVisualizer!!.captureSize = Visualizer.getCaptureSizeRange()[1]
        mVisualizer!!.setDataCaptureListener(object : OnDataCaptureListener {
            override fun onWaveFormDataCapture(
                visualizer: Visualizer, bytes: ByteArray,
                samplingRate: Int
            ) {
                  this@WaveFormView.mRawAudioBytes = bytes
                invalidate()
            }

            override fun onFftDataCapture(
                visualizer: Visualizer, bytes: ByteArray,
                samplingRate: Int
            ) {
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false)
        mVisualizer!!.enabled = true
    }

    /**
     * Releases the visualizer
     */
    fun release() {
        if (mVisualizer != null) mVisualizer!!.release()
    }
}