package com.logitech.integration.test.helpers.encoderdecoder

import android.media.MediaCodec
import android.media.MediaCodec.CodecException
import android.media.MediaCodec.CryptoException
import android.media.MediaFormat
import android.util.Log
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

private const val AUDIO_ENCODE_DEFAULT_MAX_INPUT_SIZE = 4096
private const val TAG = "Encoder"
private const val DEBUG = false
private const val kQueueDequeueTimeoutUs = 1000

class Encoder {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private var mediaCodec: MediaCodec? = null
    private var mime: String? = null
    private val stats: Stats = Stats()
    private var offset = 0
    var frameSize = 4096
    private var numInputFrame: Int = 0
    private var numFrames = 0
    private var frameRate = 0
    private var sampleRate = 0
    private var inputBufferSize: Long = 0
    private var sawInputEOS = false
    private var sawOutputEOS = false
    private var signalledError = false
    private var inputStream: FileInputStream? = null
    private var outputStream: FileOutputStream? = null
    val logger = LoggerFactory.getLogger(this.javaClass.name)


    /**
     * Setup of encoder
     *
     * @param encoderOutputStream Will dump the encoder output in this stream if not null.
     * @param fileInputStream     Will read the decoded output from this stream
     */
    fun setupEncoder(
        encoderOutputStream: FileOutputStream?,
        fileInputStream: FileInputStream?
    ) {
        inputStream = fileInputStream
        outputStream = encoderOutputStream
    }

    @Throws(IOException::class)
    private fun createCodec(codecName: String, mime: String?): MediaCodec? {
        return try {
            val codec: MediaCodec
            if (codecName.isEmpty()) {
                logger.info("$TAG Mime type: $mime")
                if (mime != null) {
                    codec = MediaCodec.createEncoderByType(mime)
                    logger.info("$TAG Encoder created for mime type $mime")
                    codec
                } else {
                    logger.error("$TAG Mime type is null, please specify a mime type to create encoder")
                    null
                }
            } else {
                codec = MediaCodec.createByCodecName(codecName)
                logger.info(
                    "$TAG Encoder created with codec name: $codecName and mime: $mime"
                )
                codec
            }
        } catch (illegalArgumentException: IllegalArgumentException) {
            logger.error(
                "$TAG Failed to create encoder for $codecName mime: $mime", illegalArgumentException
            )
            null
        }
    }

    /**
     * Encodes the given raw input file and measures the performance of encode operation,
     * provided a valid list of parameters are passed as inputs.
     *
     * @param codecName    Will create the encoder with codecName
     * @param mime         For creating encode format
     * @param encodeFormat Format of the output data
     * @param frameSize    Size of the frame
     * @param asyncMode    Will run on async implementation if true
     * @return 0 if encode was successful , -1 for fail, -2 for encoder not created
     * @throws IOException If the codec cannot be created.
     */
    @Throws(IOException::class)
    fun encode(
        codecName: String, encodeFormat: MediaFormat?, mime: String?, frameRate: Int,
        sampleRate: Int, frameSize: Int, asyncMode: Boolean
    ): Int {
        inputBufferSize = inputStream!!.channel.size()
        this.mime = mime
        offset = 0
        this.frameRate = frameRate
        this.sampleRate = sampleRate
        val sTime: Long = stats.curTime
        mediaCodec = createCodec(codecName, mime)
        if (mediaCodec == null) {
            return -2
        }
        /*Configure Codec*/try {
            mediaCodec!!.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        }
        catch (e: Exception) {
            when(e){
                is IllegalArgumentException, is IllegalStateException, is CryptoException -> {
                    logger.error("$TAG Failed to configure ${mediaCodec!!.name} encoder.", e)
                    return -2
                }
            }
        }
        if (this.mime!!.startsWith("video/")) {
            this.frameSize = frameSize
        } else {
            var maxInputSize = AUDIO_ENCODE_DEFAULT_MAX_INPUT_SIZE
            val format = mediaCodec!!.inputFormat
            if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                maxInputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            }
            this.frameSize = frameSize
            if (this.frameSize > maxInputSize && maxInputSize > 0) {
                this.frameSize = maxInputSize
            }
        }
        numFrames = ((inputBufferSize + this.frameSize - 1) / this.frameSize).toInt()
        if (asyncMode) {
            mediaCodec!!.setCallback(object : MediaCodec.Callback() {
                override fun onInputBufferAvailable(
                    mediaCodec: MediaCodec,
                    inputBufferId: Int
                ) {
                    try {
                        stats.addInputTime()
                        onInputAvailable(mediaCodec, inputBufferId)
                    } catch (exception: Exception) {
                        logger.error("$TAG ", exception)
                    }
                }

                override fun onOutputBufferAvailable(
                    mediaCodec: MediaCodec,
                    outputBufferId: Int,
                    bufferInfo: MediaCodec.BufferInfo
                ) {
                    stats.addOutputTime()
                    onOutputAvailable(mediaCodec, outputBufferId, bufferInfo)
                    if (sawOutputEOS) {
                        logger.info("$TAG Saw output EOS")
                        lock.withLock { condition.signal() }
                    }
                }

                override fun onError(mediaCodec: MediaCodec, codecException: CodecException) {
                    signalledError = true
                    logger.error("$TAG Codec Error: $codecException")
                    lock.withLock { condition.signal() }
                }

                override fun onOutputFormatChanged(
                    mediaCodec: MediaCodec,
                    format: MediaFormat
                ) {
                    logger.info(
                        "$TAG Output format changed. Format: $format"
                    )
                }
            })
        }
        mediaCodec!!.start()
        val eTime: Long = stats.curTime
        stats.initTime = (stats.getTimeDiff(sTime, eTime))
        stats.setStartTime()
        if (asyncMode) {
            try {
                lock.withLock { condition.await() }
                if (signalledError) {
                    return -1
                }
            } catch (interruptedException: InterruptedException) {
                logger.error("$TAG ", interruptedException)
            }
        } else {
            while (!sawOutputEOS && !signalledError) {
                /* Queue input data */
                if (!sawInputEOS) {
                    val inputBufferId =
                        mediaCodec!!.dequeueInputBuffer(kQueueDequeueTimeoutUs.toLong())
                    if (inputBufferId < 0 && inputBufferId != MediaCodec.INFO_TRY_AGAIN_LATER) {
                        logger.error(
                            "$TAG MediaCodec.dequeueInputBuffer returned invalid index : $inputBufferId"
                        )
                        return -1
                    }
                    stats.addInputTime()
                    onInputAvailable(mediaCodec!!, inputBufferId)
                }
                /* Dequeue output data */
                val outputBufferInfo = MediaCodec.BufferInfo()
                val outputBufferId =
                    mediaCodec!!.dequeueOutputBuffer(
                        outputBufferInfo,
                        kQueueDequeueTimeoutUs.toLong()
                    )
                when (outputBufferId < 0) {
                    outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val outFormat = mediaCodec!!.outputFormat
                        logger.info(
                            "$TAG Output format changed. Format: $outFormat"
                        )
                    }
                    outputBufferId != MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        logger.error(
                            "$TAG MediaCodec.dequeueOutputBuffer returned invalid index $outputBufferId"
                        )
                        return -1
                    }
                }
                if (outputBufferId >= 0) {
                    stats.addOutputTime()
                    if (DEBUG) {
                        logger.debug(
                            "$TAG Dequeue O/P buffer with BufferID $outputBufferId"
                        )
                    }
                    onOutputAvailable(mediaCodec!!, outputBufferId, outputBufferInfo)
                }
            }
        }
        return 0
    }

    private fun onOutputAvailable(
        mediaCodec: MediaCodec, outputBufferId: Int,
        outputBufferInfo: MediaCodec.BufferInfo
    ) {
        if (sawOutputEOS || outputBufferId < 0) {
            if (sawOutputEOS) {
                logger.info("$TAG Saw output EOS")
            }
            return
        }
        val outputBuffer = mediaCodec.getOutputBuffer(outputBufferId)
        outputStream?.let {
            try {
                val bytesOutput = ByteArray(outputBuffer!!.remaining())
                //outputBuffer[bytesOutput]
                it.write(bytesOutput)
            } catch (e: IOException) {
                e.printStackTrace()
                logger.debug("$TAG Error Dumping File: Exception $e")
                return
            }
        }
        stats.addFrameSize(outputBuffer!!.remaining())
        mediaCodec.releaseOutputBuffer(outputBufferId, false)
        sawOutputEOS = outputBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
    }

    @Throws(IOException::class)
    private fun onInputAvailable(mediaCodec: MediaCodec, inputBufferId: Int) {
        if (sawInputEOS || inputBufferId < 0) {
            if (sawInputEOS) {
                logger.info("$TAG Saw input EOS")
            }
            return
        }
        if (inputBufferSize < offset) {
            logger.error("$TAG Out of bound access of input buffer")
            signalledError = true
            return
        }
        val inputBuffer = this.mediaCodec!!.getInputBuffer(inputBufferId)
        if (inputBuffer == null) {
            signalledError = true
            return
        }
        val bufSize = inputBuffer.capacity()
        var bytesToRead = frameSize
        if (inputBufferSize - offset < frameSize) {
            bytesToRead = (inputBufferSize - offset).toInt()
        }
        //b/148655275 - Update Frame size, as Format value may not be valid
        if (bufSize < bytesToRead) {
            if (numInputFrame == 0) {
                frameSize = bufSize
                bytesToRead = bufSize
                numFrames = ((inputBufferSize + frameSize - 1) / frameSize).toInt()
            } else {
                signalledError = true
                return
            }
        }
        val inputArray = ByteArray(bytesToRead)
        inputStream!!.read(inputArray, 0, bytesToRead)
        inputBuffer.put(inputArray)
        var flag = 0
        if (numInputFrame >= numFrames - 1 || bytesToRead == 0) {
            logger.info("$TAG Sending EOS on input last frame")
            sawInputEOS = true
            flag = MediaCodec.BUFFER_FLAG_END_OF_STREAM
        }
        val presentationTimeUs: Int = if (mime!!.startsWith("video/")) {
            numInputFrame * (1000000 / frameRate)
        } else {
            numInputFrame * frameSize * 1000000 / sampleRate
        }
        mediaCodec.queueInputBuffer(
            inputBufferId,
            0,
            bytesToRead,
            presentationTimeUs.toLong(),
            flag
        )
        numInputFrame++
        offset += bytesToRead
    }

    /**
     * Stops the codec and releases codec resources.
     */
    fun deInitEncoder() {
        val beforeRelease: Long = stats.curTime
        mediaCodec?.let {
            it.stop()
            it.release()
            mediaCodec = null
        }
        val afterRelease: Long = stats.curTime
        stats.deInitTime = (stats.getTimeDiff(beforeRelease, afterRelease))
    }

    /**
     * Prints out the statistics in the information log
     *
     * @param inputReference The operation being performed, in this case decode
     * @param componentName  Name of the component/codec
     * @param mode           The operating mode: Sync/Async
     * @param durationUs     Duration of the clip in microseconds
     * @param statsFile      The output file where the stats data is written
     */
    @Throws(IOException::class)
    fun dumpStatistics(
        inputReference: String?, componentName: String?, mode: String?,
        durationUs: Long, statsFile: File?
    ) {
        val operation = "encode"
        stats.dumpStatistics(
            inputReference!!, operation, componentName!!, mode!!, durationUs, statsFile
        )
    }

    /**
     * Resets the stats
     */
    fun resetEncoder() {
        offset = 0
        inputBufferSize = 0
        numInputFrame = 0
        sawInputEOS = false
        sawOutputEOS = false
        signalledError = false
        stats.reset()
    }
}
