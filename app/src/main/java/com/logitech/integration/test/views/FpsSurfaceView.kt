package com.logitech.integration.test.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.SurfaceView
import java.util.concurrent.TimeUnit


private const val NANOS = 1000000000.0

class FpsSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr) {
    var frames  = 0L
    var lastFrame = 0L
    var slowFrames = 0L
    var startTime = 0L
    /** Calculates and returns frames per second  */
    private fun calculateFps() {
        val lastTime = TimeUnit.NANOSECONDS.convert(System.nanoTime(), TimeUnit.MILLISECONDS)
        if (frames == 0L){
            startTime = lastTime
        } else {
            val timeDiff = lastTime - lastFrame
            if (timeDiff > 16){
                slowFrames++
            }
        }
        frames ++
        lastFrame = lastTime
    }

    fun getFps(): Double{
        val timeDiff = lastFrame - startTime
        return if (timeDiff > 0) frames / TimeUnit.MILLISECONDS.convert(timeDiff, TimeUnit.SECONDS).toDouble() else 0.0
    }

    override fun onDraw(canvas: Canvas?) {
        calculateFps()
        super.onDraw(canvas)
    }
}