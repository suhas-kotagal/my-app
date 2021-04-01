package com.logitech.integration.test.helpers.encoderdecoder

import android.media.MediaCodec
import android.media.MediaFormat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.base.Stopwatch
import org.junit.Assert
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit

private const val inPutFilePath = "/data/local/tmp/KongIntegrationTest/res/"
private const val outPutFilePath = "/data/local/tmp/KongIntegrationTest/output/"
private const val TAG = "Decoder_TestKONG"
private const val WRITE_OUTPUT = false
private const val DECODE_FULLHD_INPUT = "crowd_1920x1080_25fps_4000kbps_h265.mkv"
private const val DECODE_CAMERASTREAM_INPUT = "cameraPanStream.mp4"
private val context = InstrumentationRegistry.getInstrumentation().targetContext
private val statsFile = File(context.getExternalFilesDir(null), "/Decoder.${System.currentTimeMillis()}.csv")

fun loopDecode() {
    var decoderArray = arrayOf(DecoderData(DECODE_CAMERASTREAM_INPUT), DecoderData(DECODE_FULLHD_INPUT))

    decoderArray.forEach { decoderData ->
        decoder(inPutFilePath + decoderData.inputFile, decoderData.async)
    }
}

fun decoder(inputFilePath: String, asyncMode: Boolean) {
    writeStatsHeaderToFile()
    val decoderInputFile = File(inputFilePath)
    Assert.assertTrue(
        "Cannot find $decoderInputFile in directory $inputFilePath",
        decoderInputFile.isFile
    )
    val fileInput = FileInputStream(decoderInputFile)
    val fileDescriptor = fileInput.fd
    val extractor = Extractor()
    val trackCount: Int = extractor.setUpExtractor(fileDescriptor)
    Assert.assertTrue(
        "Extraction failed. No tracks for file: $inputFilePath",
        trackCount > 0
    )
    val inputBuffer = mutableListOf<ByteBuffer>()
    val frameInfo = mutableListOf<MediaCodec.BufferInfo>()
    for (currentTrack in 0 until trackCount) {
        extractor.selectExtractorTrack(currentTrack)
        val format: MediaFormat = extractor.getFormat(currentTrack)
        val mime = format.getString(MediaFormat.KEY_MIME)
        val mediaCodecs: MutableList<String> = CodecUtils.selectCodecs(mime, false)
        Assert.assertTrue(
            "No suitable codecs found for file: $decoderInputFile track : $currentTrack mime: $mime",
            mediaCodecs.size > 0
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
            logger.debug(
                ("$TAG Extracted bufInfo: flag = ${bufInfo.flags}  timestamp =  ${bufInfo.presentationTimeUs}  size = ${bufInfo.size}")
            )

        } while (sampleSize > 0)
        mediaCodecs.forEach { codecName ->
            var decodeOutputStream: FileOutputStream? = null
            if (WRITE_OUTPUT) {
                if (!Paths.get(outPutFilePath).toFile().isFile) {
                    Files.createDirectories(Paths.get(outPutFilePath))
                }
                val outFile = File(outPutFilePath , "decoder.out")
                if (outFile.isFile) {
                    Assert.assertTrue(
                        " Unable to delete existing file $outFile",
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
            val decoderStopWatch: Stopwatch = Stopwatch.createStarted()
            val status: Int =
                decoder.decode(inputBuffer, frameInfo, asyncMode, format, codecName)
            logger.info("$TAG Time taken for decoding: ${decoderStopWatch.stop().elapsed(TimeUnit.MILLISECONDS)}")
            decoder.deInitCodec()
            Assert.assertEquals(
                ("Decoder returned error $status for file: $decoderInputFile  with codec: $codecName"),
                0,
                status.toLong()
            )
            decoder.dumpStatistics(
                inputFilePath, codecName, (if (asyncMode) "async" else "sync"),
                extractor.clipDuration, statsFile
            )
            logger.info(
                "$TAG Decoding Successful for file: $decoderInputFile with codec: $codecName"
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
    logger.debug("$TAG Suhas starting to save Benchmark results in: $statsFile")

    val stats = Stats()
    val status: Boolean = stats.writeStatsHeader(statsFile)
    Assert.assertTrue("Unable to open stats file for writing!", status)
    logger.debug("$TAG Saving Benchmark results in: $statsFile")
}
