package com.logitech.integration.test.camera.model

import android.graphics.Point
import android.media.MediaRecorder.*
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.File


enum class CAMERA(val cameraId:String){
    MAIN_CAMERA("0"),
    VIEW_FINDER("2")
}

@Parcelize
data class CameraRecordOptions (
    val outputFileName : File? = null,
    val videoEncoding : Int = VideoEncoder.H264,
    val audioEncoding : Int = AudioEncoder.AAC,
    val videoBitRate: Int = 10_000_000,
    val outputFormat: Int = OutputFormat.MPEG_4
) : Parcelable


@Parcelize
data class CameraSettings (
        val fps:Int,
        val cameraId: String = CAMERA.MAIN_CAMERA.cameraId,
        val videoSize: Point = Point(1920, 1080),
        val cameraRecordOptions: CameraRecordOptions? = null,
        val autoFocus : Boolean = false
    ) : Parcelable {
    companion object{
        const val EXTRA_NAME = "camera.settings"
    }
}
