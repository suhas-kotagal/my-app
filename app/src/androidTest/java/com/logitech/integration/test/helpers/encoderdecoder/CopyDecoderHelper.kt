/*
package com.logitech.integration.test.helpers.encoderdecoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.logitech.integration.test.camera.CameraPtzTestBlueShell
//import com.logitech.integration.test.camera.CameraPtzTestBlueShell.Companion.start
import com.logitech.integration.test.camera.DecoderTest
import com.logitech.integration.test.common.CodecUtils
import com.logitech.integration.test.helpers.encoderdecoder.Decoder
import com.logitech.integration.test.helpers.encoderdecoder.Extractor
import com.logitech.integration.test.helpers.encoderdecoder.Stats
import org.junit.Assert
import org.junit.runners.Parameterized
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

private val mContext = InstrumentationRegistry.getInstrumentation().targetContext
private val mInputFilePath = "/data/local/tmp/KongIntegrationTest/res/"
private val mOutputFilePath = "/data/local/tmp/KongIntegrationTest/output/"
private val mStatsFile = mContext.getExternalFilesDir(null)
    .toString() + "/Decoder." + System.currentTimeMillis() + ".csv"
private val TAG = "Decoder_TestKONG"
private val PER_TEST_TIMEOUT_MS: Long = 60000
private val WRITE_OUTPUT = false

var array =

    arrayOf(
        arrayOf("cameraPanStream.mp4", true))
    */
/*arrayOf(
        arrayOf("bbb_44100hz_2ch_128kbps_aac_30sec.mp4", false),
        arrayOf("bbb_44100hz_2ch_128kbps_mp3_30sec.mp3", false),
        arrayOf("bbb_8000hz_1ch_8kbps_amrnb_30sec.3gp", false),
        arrayOf("bbb_16000hz_1ch_9kbps_amrwb_30sec.3gp", false),
        arrayOf("bbb_44100hz_2ch_80kbps_vorbis_30sec.webm", false),
        arrayOf("bbb_44100hz_2ch_600kbps_flac_30sec.mp4", false),
        arrayOf("bbb_48000hz_2ch_100kbps_opus_30sec.webm", false),
        arrayOf("bbb_44100hz_2ch_128kbps_aac_30sec.mp4", true),
        arrayOf("bbb_44100hz_2ch_128kbps_mp3_30sec.mp3", true),
        arrayOf("bbb_8000hz_1ch_8kbps_amrnb_30sec.3gp", true),
        arrayOf("bbb_16000hz_1ch_9kbps_amrwb_30sec.3gp", true),
        arrayOf("bbb_44100hz_2ch_80kbps_vorbis_30sec.webm", true),
        arrayOf("bbb_44100hz_2ch_600kbps_flac_30sec.mp4", true),
        arrayOf("bbb_48000hz_2ch_100kbps_opus_30sec.webm", true),
        arrayOf("crowd_1920x1080_25fps_4000kbps_vp9.webm", false),
        arrayOf("crowd_1920x1080_25fps_4000kbps_vp8.webm", false),
        arrayOf("crowd_1920x1080_25fps_4000kbps_av1.webm", false),
        arrayOf("crowd_1920x1080_25fps_7300kbps_mpeg2.mp4", false),
        arrayOf("crowd_1920x1080_25fps_6000kbps_mpeg4.mp4", false),
        arrayOf("crowd_352x288_25fps_6000kbps_h263.3gp", false),
        arrayOf("crowd_1920x1080_25fps_6700kbps_h264.ts", false),
        arrayOf("crowd_1920x1080_25fps_4000kbps_h265.mkv", false),
        arrayOf("crowd_1920x1080_25fps_4000kbps_vp9.webm", true),
        arrayOf("crowd_1920x1080_25fps_4000kbps_vp8.webm", true),
        arrayOf("crowd_1920x1080_25fps_4000kbps_av1.webm", true),
        arrayOf("crowd_1920x1080_25fps_7300kbps_mpeg2.mp4", true),
        arrayOf("crowd_1920x1080_25fps_6000kbps_mpeg4.mp4", true),
        arrayOf("crowd_352x288_25fps_6000kbps_h263.3gp", true),
        arrayOf("crowd_1920x1080_25fps_6700kbps_h264.ts", true),
        arrayOf("crowd_1920x1080_25fps_4000kbps_h265.mkv", true)
    )*//*



fun loopDecode() {
    for (arrays in array) {
        //if(start.get()){
            Log.d("KONGINTEGRATION", "running decode now")
            decodeFiles(mInputFilePath + arrays[0] as String, true)
        //}
        //else Log.d("KONGINTEGRATION", "not running decode")
    }
}


fun decodeFiles(mInputFile: String, mAsyncMode: Boolean) {
    writeStatsHeaderToFile()
    val inputFile = File(mInputFile)
    Assert.assertTrue(
        "Cannot find " + mInputFile + " in directory " + mInputFilePath,
        inputFile.exists()
    )
    val fileInput = FileInputStream(inputFile)
    val fileDescriptor = fileInput.fd
    val extractor = Extractor()
    val trackCount: Int = extractor.setUpExtractor(fileDescriptor)
    Assert.assertTrue(
        "Extraction failed. No tracks for file: $mInputFile",
        trackCount > 0
    )
    val inputBuffer = ArrayList<ByteBuffer>()
    val frameInfo = ArrayList<MediaCodec.BufferInfo>()
    for (currentTrack in 0 until trackCount) {
        extractor.selectExtractorTrack(currentTrack)
        val format: MediaFormat = extractor.getFormat(currentTrack)
        val mime = format.getString(MediaFormat.KEY_MIME)
        val mediaCodecs: ArrayList<String> = CodecUtils.selectCodecs(mime, false)
        Assert.assertTrue(
            "No suitable codecs found for file: " + mInputFile + " track : " +
                    currentTrack + " mime: " + mime, (mediaCodecs.size > 0)
        )

        // Get samples from extractor
        var sampleSize: Int
        do {
            sampleSize = extractor.frameSample
            val bufInfo = MediaCodec.BufferInfo()
            val info: MediaCodec.BufferInfo = extractor.bufferInfo
            val dataBuffer = ByteBuffer.allocate(info.size)
            dataBuffer.put(extractor.frameBuffer.array(), 0, info.size)
            bufInfo[info.offset, info.size, info.presentationTimeUs] = info.flags
            inputBuffer.add(dataBuffer)
            frameInfo.add(bufInfo)
                Log.d(
                    TAG, ("Extracted bufInfo: flag = " + bufInfo.flags + " timestamp = " +
                            bufInfo.presentationTimeUs + " size = " + bufInfo.size)
                )

        } while (sampleSize > 0)
        for (codecName: String in mediaCodecs) {
            var decodeOutputStream: FileOutputStream? = null
            if (WRITE_OUTPUT) {
                if (!Paths.get(mOutputFilePath).toFile().exists()) {
                    Files.createDirectories(
                        Paths.get(
                            mOutputFilePath
                        )
                    )
                }
                val outFile = File(mOutputFilePath + "decoder.out")
                if (outFile.exists()) {
                    Assert.assertTrue(
                        " Unable to delete existing file$outFile",
                        outFile.delete()
                    )
                }
                Assert.assertTrue(
                    "Unable to create file: $outFile",
                    outFile.createNewFile()
                )
                decodeOutputStream = FileOutputStream(outFile)
            }
            val decoder = Decoder()
            decoder.setupDecoder(decodeOutputStream)
            val status: Int =
                decoder.decode(inputBuffer, frameInfo, mAsyncMode, format, codecName)
            decoder.deInitCodec()
            Assert.assertEquals(
                ("Decoder returned error " + status + " for file: " + mInputFile +
                        " with codec: " + codecName), 0, status.toLong()
            )
            decoder.dumpStatistics(
                mInputFile, codecName, (if (mAsyncMode) "async" else "sync"),
                extractor.clipDuration, mStatsFile
            )
            Log.i(
                TAG, ("Decoding Successful for file: " + mInputFile + " with codec: " +
                        codecName)
            )
            decoder.resetDecoder()
            decodeOutputStream?.close()
        }
        extractor.unselectExtractorTrack(currentTrack)
        inputBuffer.clear()
        frameInfo.clear()
    }
    extractor.deinitExtractor()
    fileInput.close()
}

fun writeStatsHeaderToFile() {
    val mStats = Stats()
    val status: Boolean = mStats.writeStatsHeader(mStatsFile)
    Assert.assertTrue("Unable to open stats file for writing!", status)
    Log.d(TAG, "Saving Benchmark results in: " + mStatsFile)
}
*/
