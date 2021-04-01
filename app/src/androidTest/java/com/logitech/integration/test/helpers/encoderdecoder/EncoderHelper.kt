package com.logitech.integration.test.helpers.encoderdecoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.base.Stopwatch
import org.junit.Assert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

private const val inPutFilePath = "/data/local/tmp/KongIntegrationTest/res/"
private const val outPutFilePath = "/data/local/tmp/KongIntegrationTest/output/"
private const val TAG = "Encoder_TestKONG"
private const val WRITE_OUTPUT = false
private const val ENCODE_DEFAULT_FRAME_RATE = 25
private const val ENCODE_MIN_VIDEO_BIT_RATE = 600000 /* 600 Kbps */
private const val DECODE_FULLHD_INPUT = "crowd_1920x1080_25fps_4000kbps_h265.mkv"
private const val DECODE_CAMERASTREAM_INPUT = "cameraPanStream.mp4"
private const val DECODE_FULLHD_UNPACKED = "crowd_1920x1080_25fps_4000kbps_h265.yuv"
private const val DECODE_CAMERA_UNPACKED = "camera_pan_stream.yuv"
private val context = InstrumentationRegistry.getInstrumentation().targetContext
private val fileDirPath = "${context.filesDir}/"
private val statsFile = File(
    context.getExternalFilesDir(null),
    "/Encoder.${System.currentTimeMillis()}.csv"
)
private var colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
private var decodedFileFullHd: File? = null
private var decodedFileCamera: File? = null
val logger: Logger = LoggerFactory.getLogger("com.logitech.integration.test.helpers.encoderdecoder")
private var frameSizeData: Int = 0

fun loopEncode() {
    var encoderArray =
        arrayOf(EncoderData(DECODE_FULLHD_UNPACKED), EncoderData(DECODE_CAMERA_UNPACKED))

    encoderArray.forEach { encodeData ->
        logger.info("Suhas encoding now..")
        prepareInput()
        encoder(encodeData)
    }
}

fun encoder(encoderData: EncoderData) {
    writeStatsHeaderToFile()

    var status: Int
    val mediaCodecs: MutableList<String> = CodecUtils.selectCodecs(encoderData.mime, true)
    Assert.assertTrue(
        "No suitable codecs found for mimetype: ${encoderData.mime}",
        mediaCodecs.size > 0
    )
    val encodeMode = arrayOf(true, false)
    // Encoding the decoded input file
    encodeMode.forEach { asyncMode ->
        mediaCodecs.forEach { codecName ->
            var encodeOutputStream: FileOutputStream? = null
            if (WRITE_OUTPUT) {
                val outEncodeFile = File(outPutFilePath, "encoder.out")
                if (outEncodeFile.isFile) {
                    Assert.assertTrue(
                        " Unable to delete existing file $outEncodeFile",
                        outEncodeFile.delete()
                    )
                }
                Assert.assertTrue(
                    "Unable to create file to write encoder output: $outEncodeFile",
                    outEncodeFile.createNewFile()

                )
                encodeOutputStream = FileOutputStream(outEncodeFile)
            }
            val rawFile = File(fileDirPath, encoderData.inputFile)
            logger.info(
                "$TAG Path of decoded input file: $rawFile"
            )

            Assert.assertTrue("Cannot open decoded input file", rawFile.isFile)
            logger.info(
                "$TAG Path of decoded input file: $rawFile"
            )

            val eleStream = FileInputStream(rawFile)
            // Setup Encode Format
            val encoder = Encoder()
            var encodeFormat = encoder.setUpEncoderDataFormat(encoderData)
                encoder.setupEncoder(encodeOutputStream, eleStream)
            val encoderStopWatch: Stopwatch = Stopwatch.createStarted()
            status = encoder.encode(
                codecName, encodeFormat, encoderData.mime, ENCODE_DEFAULT_FRAME_RATE,
                encoderData.sampleRate, frameSizeData, asyncMode
            )
            logger.info("$TAG Time taken for encoding: ${encoderStopWatch.stop().elapsed(TimeUnit.MILLISECONDS)}")
            encoder.deInitEncoder()
            Assert.assertEquals(
                "$codecName encoder returned error $status or mime: ${encoderData.mime}",
                0,
                status.toLong()
            )
            var inputReference: String
            var durationUs: Long
            if (encoderData.mime.startsWith("video/")) {
                inputReference =
                    "${encoderData.inputFile} ${encoderData.pixelWidth} x ${encoderData.pixelHeight} ${encoderData.videoBitRate} bps"
                durationUs = (((eleStream.channel.size() + frameSizeData - 1) / frameSizeData) /
                        ENCODE_DEFAULT_FRAME_RATE) * 1000000
            } else {
                inputReference = ("${encoderData.inputFile} ${encoderData.sampleRate} hz_${encoderData.numChannel} ch_${encoderData.videoBitRate} bps")
                durationUs = (eleStream.channel.size() / (encoderData.sampleRate * encoderData.numChannel)) * 1000000
            }
            encoder.dumpStatistics(
                inputReference, codecName, (if (asyncMode) "async" else "sync"),
                durationUs, statsFile
            )
            logger.info(
                ("$TAG Encoding complete for mime: ${encoderData.mime} with codec: $codecName for aSyncMode = $asyncMode")
            )
            encoder.resetEncoder()
            eleStream.close()
            encodeOutputStream?.close()
        }
    }
    deleteDecodedFiles()
}

fun prepareInput() {
    decodedFileFullHd = File(fileDirPath + DECODE_FULLHD_UNPACKED)
    var status = decodeFile(inPutFilePath + DECODE_FULLHD_INPUT, decodedFileFullHd)
    Assert.assertEquals("Decoder returned error $status", 0, status.toLong())

    decodedFileCamera = File(fileDirPath + DECODE_CAMERA_UNPACKED)
    status = decodeFile(inPutFilePath + DECODE_CAMERASTREAM_INPUT, decodedFileCamera)
    Assert.assertEquals("Decoder returned error $status", 0, status.toLong())
}

private fun decodeFile(inputFileName: String, outputDecodeFile: File?): Int {
    var status = -1
    val inputFile = File(inputFileName)
    Assert.assertTrue("Cannot open input file $inputFileName", inputFile.isFile)
    val fileInput = FileInputStream(inputFile)
    val fileDescriptor = fileInput.fd
    val decodeOutputStream = FileOutputStream(outputDecodeFile)
    val extractor = Extractor()
    val trackCount: Int = extractor.setUpExtractor(fileDescriptor)
    Assert.assertTrue(
        "Extraction failed. No tracks for the given input file",
        (trackCount > 0)
    )
    val inputBuffer = mutableListOf<ByteBuffer>()
    val frameInfo = mutableListOf<MediaCodec.BufferInfo>()

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
            logger.debug(
                ("$TAG Extracted bufInfo: flag = ${bufInfo.flags} timestamp = ${bufInfo.presentationTimeUs} size = ${bufInfo.size}")
            )

        } while (sampleSize > 0)
        val decoder = Decoder()
        decoder.setupDecoder(decodeOutputStream)
        status = decoder.decode(inputBuffer, frameInfo, false, format, "")
        val decoderFormat: MediaFormat = decoder.format
        if (decoderFormat.containsKey(MediaFormat.KEY_COLOR_FORMAT)) {
            colorFormat = decoderFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT)
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
    if (decodedFileFullHd!!.exists()) {
        Assert.assertTrue(
            " Unable to delete decoded file ${decodedFileFullHd.toString()}",
            decodedFileFullHd!!.delete()
        )
        logger.info("$TAG Successfully deleted decoded file ${decodedFileFullHd.toString()}")
    }

    if (decodedFileCamera!!.exists()) {
        Assert.assertTrue(
            " Unable to delete decoded file ${decodedFileCamera.toString()}",
            decodedFileCamera!!.delete()
        )
        logger.info("$TAG Successfully deleted decoded file ${decodedFileCamera.toString()}")
    }
}

fun Encoder.setUpEncoderDataFormat(encoderData: EncoderData) : MediaFormat{
    var encodeFormat: MediaFormat
    if (encoderData.mime.startsWith("video/")) {
        frameSizeData = encoderData.pixelWidth * encoderData.pixelHeight * 3 / 2
        encodeFormat = MediaFormat.createVideoFormat(
            encoderData.mime,
            encoderData.pixelWidth,
            encoderData.pixelHeight
        )
        encodeFormat.setInteger(
            MediaFormat.KEY_FRAME_RATE,
            ENCODE_DEFAULT_FRAME_RATE
        )
        encodeFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, encoderData.frameInterval)
        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, encoderData.videoBitRate)
        encodeFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
        if (encoderData.profile != -1 && encoderData.level != -1) {
            encodeFormat.setInteger(MediaFormat.KEY_PROFILE, encoderData.profile)
            encodeFormat.setInteger(MediaFormat.KEY_LEVEL, encoderData.level)
        }
    } else {
        frameSizeData = 4096
        encodeFormat = MediaFormat.createAudioFormat(
            encoderData.mime,
            encoderData.sampleRate,
            encoderData.numChannel
        )
        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, encoderData.videoBitRate)
    }
    return encodeFormat

}