/*
package com.logitech.integration.test.helpers.encoderdecoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
//import com.logitech.integration.test.camera.CameraPtzTestBlueShell.Companion.start
import com.logitech.integration.test.camera.EncoderTest
import com.logitech.integration.test.common.CodecUtils
import com.logitech.integration.test.helpers.encoderdecoder.Decoder
import com.logitech.integration.test.helpers.common.Encoder
import com.logitech.integration.test.helpers.encoderdecoder.Extractor
import org.junit.Assert
import org.junit.runners.Parameterized
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.*

private val mContext = InstrumentationRegistry.getInstrumentation().targetContext
private val mFileDirPath = mContext.filesDir.toString() + "/"
private const val mInputFilePath = "/data/local/tmp/KongIntegrationTest/res/"
private const val mOutputFilePath = "/data/local/tmp/KongIntegrationTest/output/"
private val mStatsFile = mContext.getExternalFilesDir(null)
    .toString() + "/Encoder." + System.currentTimeMillis() + ".csv"
private val TAG = "Encoder_TestKONG"
private val WRITE_OUTPUT = false
private val PER_TEST_TIMEOUT_MS: Long = 120000
private val ENCODE_DEFAULT_FRAME_RATE = 25
private val ENCODE_DEFAULT_VIDEO_BIT_RATE = 8000000 */
/* 8 Mbps *//*

private val ENCODE_MIN_VIDEO_BIT_RATE = 600000 */
/* 600 Kbps *//*

public val ENCODE_DEFAULT_AUDIO_BIT_RATE = 128000 */
/* 128 Kbps *//*

private var mColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
private var mDecodedFileQcif: File? = null
private var mDecodedFileFullHd: File? = null
private var mDecodedFileAudio: File? = null
private var mDecodedFileCamera: File? = null
private val DECODE_FULLHD_INPUT = "crowd_1920x1080_25fps_4000kbps_h265.mkv"
private val DECODE_CAMERASTREAM_INPUT = "cameraPanStream.mp4"
private val DECODE_QCIF_INPUT = "crowd_176x144_25fps_6000kbps_mpeg4.mp4"
private val DECODE_AUDIO_INPUT = "bbb_48000hz_2ch_100kbps_opus_30sec.webm"
private val DECODE_FULLHD_UNPACKED = "crowd_1920x1080_25fps_4000kbps_h265.yuv"
private val DECODE_QCIF_UNPACKED = "crowd_176x144_25fps_6000kbps_mpeg4.yuv"
private val DECODE_AUDIO_UNPACKED = "bbb_48000hz_2ch_100kbps_opus_30sec.raw"
public val DECODE_CAMERA_UNPACKED = "camera_1920x1080.yuv"

var arrays =

    arrayOf(
        arrayOf(
            DECODE_FULLHD_UNPACKED, MediaFormat.MIMETYPE_VIDEO_VP8,
            ENCODE_MIN_VIDEO_BIT_RATE, 1920, 1080, 1, -1, -1, -1, -1
        )
    )


    */
/*arrayOf(
    arrayOf(
        DECODE_AUDIO_UNPACKED, MediaFormat.MIMETYPE_AUDIO_AAC,
        ENCODE_DEFAULT_AUDIO_BIT_RATE, -1, -1, -1, -1, -1, 44100, 2
    ), arrayOf(
        DECODE_AUDIO_UNPACKED, MediaFormat.MIMETYPE_AUDIO_AMR_NB,
        ENCODE_DEFAULT_AUDIO_BIT_RATE, -1, -1, -1, -1, -1, 8000, 1
    ),
    arrayOf(
        DECODE_AUDIO_UNPACKED, MediaFormat.MIMETYPE_AUDIO_AMR_WB,
        ENCODE_DEFAULT_AUDIO_BIT_RATE, -1, -1, -1, -1, -1, 16000, 1
    ), arrayOf(
        DECODE_AUDIO_UNPACKED, MediaFormat.MIMETYPE_AUDIO_FLAC,
        ENCODE_DEFAULT_AUDIO_BIT_RATE, -1, -1, -1, -1, -1, 44100, 2
    ), arrayOf(
        DECODE_AUDIO_UNPACKED, MediaFormat.MIMETYPE_AUDIO_OPUS,
        ENCODE_DEFAULT_AUDIO_BIT_RATE, -1, -1, -1, -1, -1, 48000, 2
    ), arrayOf(
        DECODE_FULLHD_UNPACKED, MediaFormat.MIMETYPE_VIDEO_VP8,
        ENCODE_DEFAULT_VIDEO_BIT_RATE, 1920, 1080, 1, -1, -1, -1, -1
    ), arrayOf(
        DECODE_FULLHD_UNPACKED, MediaFormat.MIMETYPE_VIDEO_AVC,
        ENCODE_DEFAULT_VIDEO_BIT_RATE, 1920, 1080, 1, -1, -1, -1, -1
    ), arrayOf(
        DECODE_FULLHD_UNPACKED, MediaFormat.MIMETYPE_VIDEO_HEVC,
        ENCODE_DEFAULT_VIDEO_BIT_RATE, 1920, 1080, 1, -1, -1, -1, -1
    ), arrayOf(
        DECODE_FULLHD_UNPACKED, MediaFormat.MIMETYPE_VIDEO_VP9,
        ENCODE_DEFAULT_VIDEO_BIT_RATE, 1920, 1080, 1, -1, -1, -1, -1
    ), arrayOf(
        DECODE_QCIF_UNPACKED,
        MediaFormat.MIMETYPE_VIDEO_MPEG4,
        ENCODE_MIN_VIDEO_BIT_RATE,
        176,
        144,
        1,
        -1,
        -1,
        -1,
        -1
    ), arrayOf(
        DECODE_QCIF_UNPACKED,
        MediaFormat.MIMETYPE_VIDEO_H263,
        ENCODE_MIN_VIDEO_BIT_RATE,
        176,
        144,
        1,
        -1,
        -1,
        -1,
        -1
    )
)*//*


fun loopEncode() {
    for (array in arrays) {
        //if (start.get()) {
            Log.d("KONGINTEGRATION", "running encode now.")
        prepareInput()
            encodeIt(
                array[0] as String,
                array[1] as String,
                array[2] as Int,
                array[3] as Int,
                array[4] as Int,
                array[5] as Int,
                array[6] as Int,
                array[7] as Int,
                array[8] as Int,
                array[9] as Int
            )

        //}
        //else Log.d("KONGINTEGRATION", "not running encode")

    }
}


fun encodeIt(
    mInputFile: String,
    mMime: String,
    mBitRate: Int,
    mWidth: Int,
    mHeight: Int,
    mIFrameInterval: Int,
    mProfile: Int,
    mLevel: Int,
    mSampleRate: Int,
    mNumChannel: Int
) {
    writeStatsHeaderToFile()

    var status: Int
    var frameSize: Int
    val mediaCodecs: ArrayList<String> = CodecUtils.selectCodecs(mMime, true)
    Assert.assertTrue(
        "No suitable codecs found for mimetype: $mMime",
        mediaCodecs.size > 0
    )
    val encodeMode = arrayOf(true, false)
    // Encoding the decoded input file
    for (asyncMode: Boolean in encodeMode) {
        for (codecName: String in mediaCodecs) {
            var encodeOutputStream: FileOutputStream? = null
            if (WRITE_OUTPUT) {
                val outEncodeFile = File(mOutputFilePath + "encoder.out")
                if (outEncodeFile.exists()) {
                    Assert.assertTrue(
                        " Unable to delete existing file$outEncodeFile",
                        outEncodeFile.delete()
                    )
                }
                Assert.assertTrue(
                    "Unable to create file to write encoder output: " +
                            outEncodeFile.toString(), outEncodeFile.createNewFile()
                )
                encodeOutputStream = FileOutputStream(outEncodeFile)
            }
            val rawFile = File(mFileDirPath + mInputFile)
            Log.i(
                TAG,
                "Path of decoded input file: $rawFile"
            )

            Assert.assertTrue("Cannot open decoded input file", rawFile.exists())
            Log.i(
                TAG,
                "Path of decoded input file: $rawFile"
            )

            val eleStream = FileInputStream(rawFile)
            // Setup Encode Format
            var encodeFormat: MediaFormat
            if (mMime.startsWith("video/")) {
                frameSize = mWidth * mHeight * 3 / 2
                encodeFormat = MediaFormat.createVideoFormat(mMime, mWidth, mHeight)
                encodeFormat.setInteger(
                    MediaFormat.KEY_FRAME_RATE,
                    ENCODE_DEFAULT_FRAME_RATE
                )
                encodeFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mIFrameInterval)
                encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate)
                encodeFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat)
                if (mProfile != -1 && mLevel != -1) {
                    encodeFormat.setInteger(MediaFormat.KEY_PROFILE, mProfile)
                    encodeFormat.setInteger(MediaFormat.KEY_LEVEL, mLevel)
                }
            } else {
                frameSize = 4096
                encodeFormat = MediaFormat.createAudioFormat(mMime, mSampleRate, mNumChannel)
                encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate)
            }
            val encoder = Encoder()
            encoder.setupEncoder(encodeOutputStream, eleStream)
            status = encoder.encode(
                codecName, encodeFormat, mMime, ENCODE_DEFAULT_FRAME_RATE,
                mSampleRate, frameSize, asyncMode
            )
            encoder.deInitEncoder()
            Assert.assertEquals(
                codecName + " encoder returned error " + status + " for " + "mime:" + " " +
                        mMime, 0, status.toLong()
            )
            var inputReference: String
            var durationUs: Long
            if (mMime.startsWith("video/")) {
                inputReference =
                    mInputFile + "_" + mWidth + "x" + mHeight + "_" + mBitRate + "bps"
                durationUs = (((eleStream.channel.size() + frameSize - 1) / frameSize) /
                        ENCODE_DEFAULT_FRAME_RATE) * 1000000
            } else {
                inputReference = (mInputFile + "_" + mSampleRate + "hz_" + mNumChannel + "ch_" +
                        mBitRate + "bps")
                durationUs = (eleStream.channel.size() / (mSampleRate * mNumChannel)) * 1000000
            }
            encoder.dumpStatistics(
                inputReference, codecName, (if (asyncMode) "async" else "sync"),
                durationUs, mStatsFile
            )
            Log.i(
                TAG, ("Encoding complete for mime: " + mMime + " with codec: " + codecName +
                        " for aSyncMode = " + asyncMode)
            )
            encoder.resetEncoder()
            eleStream.close()
            encodeOutputStream?.close()
        }
    }
    deleteDecodedFiles()
}

fun prepareInput() {
    mDecodedFileFullHd = File(mFileDirPath + DECODE_FULLHD_UNPACKED)
    var status = decodeFile(mInputFilePath + DECODE_CAMERASTREAM_INPUT, mDecodedFileFullHd)
    Assert.assertEquals("Decoder returned error $status", 0, status.toLong())
    */
/*mDecodedFileQcif = File(mFileDirPath + DECODE_QCIF_UNPACKED)
    status = decodeFile(mInputFilePath + DECODE_QCIF_INPUT, mDecodedFileQcif)
    Assert.assertEquals("Decoder returned error $status", 0, status.toLong())
    mDecodedFileAudio = File(mFileDirPath + DECODE_AUDIO_UNPACKED)
    status = decodeFile(mInputFilePath + DECODE_AUDIO_INPUT, mDecodedFileAudio)
    Assert.assertEquals("Decoder returned error $status", 0, status.toLong())
*//*
}

fun prepareCameraInput(cameraInputFile : String) {
    mDecodedFileCamera = File(mFileDirPath + DECODE_CAMERA_UNPACKED)
    var status = decodeFile(cameraInputFile, mDecodedFileCamera)
    Assert.assertEquals("Decoder returned error $status", 0, status.toLong())
}

private fun decodeFile(inputFileName: String, outputDecodeFile: File?): Int {
    var status = -1
    val inputFile = File(inputFileName)
    Assert.assertTrue("Cannot open input file $inputFileName", inputFile.exists())
    val fileInput = FileInputStream(inputFile)
    val fileDescriptor = fileInput.fd
    val decodeOutputStream = FileOutputStream(outputDecodeFile)
    val extractor = Extractor()
    val trackCount: Int = extractor.setUpExtractor(fileDescriptor)
    Assert.assertTrue(
        "Extraction failed. No tracks for the given input file",
        (trackCount > 0)
    )
    val inputBuffer = ArrayList<ByteBuffer>()
    val frameInfo = ArrayList<MediaCodec.BufferInfo>()
    for (currentTrack in 0 until trackCount) {
        extractor.selectExtractorTrack(currentTrack)
        val format: MediaFormat = extractor.getFormat(currentTrack)
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
        val decoder = Decoder()
        decoder.setupDecoder(decodeOutputStream)
        status = decoder.decode(inputBuffer, frameInfo, false, format, "")
        val decoderFormat: MediaFormat = decoder.format
        if (decoderFormat.containsKey(MediaFormat.KEY_COLOR_FORMAT)) {
            mColorFormat = decoderFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT)
        }
        decoder.deInitCodec()
        extractor.unselectExtractorTrack(currentTrack)
        inputBuffer.clear()
        frameInfo.clear()
    }
    extractor.deinitExtractor()
    fileInput.close()
    decodeOutputStream.close()
    return status
}

fun deleteDecodedFiles() {
    */
/*   if (mDecodedFileFullHd!!.exists()) {
           Assert.assertTrue(
               " Unable to delete decoded file" + mDecodedFileFullHd.toString(),
               mDecodedFileFullHd!!.delete()
           )
           Log.i(TAG, "Successfully deleted decoded file" + mDecodedFileFullHd.toString())
       }

       if (mDecodedFileCamera!!.exists()) {
           Assert.assertTrue(
               " Unable to delete decoded file" + mDecodedFileCamera.toString(),
               mDecodedFileCamera!!.delete()
           )
           Log.i(TAG, "Successfully deleted decoded file" + mDecodedFileCamera.toString())
       }*//*


*/
/*    if (mDecodedFileQcif!!.exists()) {
        Assert.assertTrue(
            " Unable to delete decoded file" + mDecodedFileQcif.toString(),
            mDecodedFileQcif!!.delete()
        )
        Log.i(TAG, "Successfully deleted decoded file" + mDecodedFileQcif.toString())
    }*//*

*/
/*    if (mDecodedFileAudio!!.exists()) {
        Assert.assertTrue(
            " Unable to delete decoded file" + mDecodedFileAudio.toString(),
            mDecodedFileAudio!!.delete()
        )
        Log.i(TAG, "Successfully deleted decoded file" + mDecodedFileAudio.toString())
    }*//*

}
*/
