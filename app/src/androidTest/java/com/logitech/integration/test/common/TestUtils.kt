package com.logitech.integration.test.common

import android.content.Context
import android.graphics.Point
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


enum class VideoSize(val point:Point){
    VID_640_480(Point(640, 360)),
    VID_1920_1080(Point(1920, 1080))
}

fun createVideoFile(context: Context, extension: String): File {
    val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
    return File(context.filesDir, "VID_${sdf.format(Date())}.$extension")
}