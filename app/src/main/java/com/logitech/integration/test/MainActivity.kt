package com.logitech.integration.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.logitech.integration.test.camera.CameraFragment
import com.logitech.integration.test.camera.model.CameraResults
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.thdev.mediacodecexample.video.VideoDecodeThread
import android.os.AsyncTask as AsyncTask1

class MainActivity : AppCompatActivity(), CameraFragment.OnCameraData {
    var cameraRecordResults : CameraResults? = null
    private var videoDecode: VideoDecodeThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //startEncoderVideo()
        //Thread {
            //startEncoderVideo()
        //}
    }

    fun startEncoderVideo() {
        //for(i in 1..10){
            val videoPath =  applicationContext.resources.openRawResourceFd(R.raw.sample_video)
            videoDecode = VideoDecodeThread()
            var b = videoDecode?.init(null, videoPath)
            Log.d("VideoDecoderKONG", "the value is: $b")
            if (b == true) {
Log.d("VideoDecoderKONG", "started")
                videoDecode?.start()

            } else {
                videoDecode = null
            }
       // }
    }


    override fun onRecordComplete(results: CameraResults) {
        cameraRecordResults = results
        val intent = Intent()
        intent.putExtra(CameraResults.EXTRA, results)
        this.setResult(CameraResults.RESULT_CODE, intent)
        finish()
    }

    override fun onPause() {
        super.onPause()


    }
}
