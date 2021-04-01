package com.logitech.integration.test.helpers.encoderdecoder

import android.media.MediaCodec
import android.media.MediaCodec.CodecException
import android.media.MediaFormat
import android.util.Log
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/*
* Copyright (C) 2019 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

private const val TAG = "Decoder"
private const val DEBUG = false
private const val kQueueDequeueTimeoutUs = 1000

class Decoder() {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private var mediaCodec: MediaCodec? = null
    private var inputBufferInfo: MutableList<MediaCodec.BufferInfo>? = null
    private val stats: Stats = Stats()
    private var sawInputEOS = false
    private var sawOutputEOS = false
    private var signalledError = false
    private var numOutputFrame = 0
    private var index = 0
    private var inputBuffer: MutableList<ByteBuffer>? = null
    private var outputStream: FileOutputStream? = null
    val logger = LoggerFactory.getLogger(this.javaClass.name)


    /**
     * Setup of decoder
     *
     * @param outputStream Will dump the output in this stream if not null.
     */
    fun setupDecoder(outputStream: FileOutputStream?) {
        signalledError = false
        this.outputStream = outputStream
    }

    @Throws(IOException::class)
    private fun createCodec(codecName: String, format: MediaFormat): MediaCodec? {
        val mime = format.getString(MediaFormat.KEY_MIME)
        try {
            val codec: MediaCodec
            if (codecName.isEmpty()) {
                logger.info(
                    "$TAG File mime type: $mime"
                )
                return if (mime != null) {
                    codec = MediaCodec.createDecoderByType(mime)
                    logger.info(
                        "$TAG Decoder created for mime type $mime"
                    )
                    codec
                } else {
                    logger.error(
                        "$TAG Mime type is null, please specify a mime type to create decoder"
                    )
                    null
                }
            } else {
                codec = MediaCodec.createByCodecName(codecName)
                logger.info(
                    "$TAG Decoder created with codec name: $codecName mime: $mime"
                )
                return codec
            }
        } catch (illegalArgumentException: IllegalArgumentException) {
            logger.error(
                "$TAG Failed to create decoder for $codecName mime:$mime", illegalArgumentException
            )
            return null
        }
    }

    /**
     * Decodes the given input buffer,
     * provided valid list of buffer info and format are passed as inputs.
     *
     * @param inputBuffer     Decode the provided list of ByteBuffers
     * @param inputBufferInfo List of buffer info corresponding to provided input buffers
     * @param asyncMode       Will run on async implementation if true
     * @param format          For creating the decoder if codec name is empty and configuring it
     * @param codecName       Will create the decoder with codecName
     * @return 0 if decode was successful , -1 for fail, -2 for decoder not created
     * @throws IOException if the codec cannot be created.
     */
    @Throws(IOException::class)
    fun decode(
        inputBuffer: MutableList<ByteBuffer>,
        inputBufferInfo: MutableList<MediaCodec.BufferInfo>, asyncMode: Boolean,
        format: MediaFormat, codecName: String
    ): Int {
        this.inputBuffer = ArrayList(inputBuffer.size)
        this.inputBuffer!!.addAll(inputBuffer)
        this.inputBufferInfo = ArrayList(inputBufferInfo.size)
        this.inputBufferInfo!!.addAll(inputBufferInfo)
        sawInputEOS = false
        sawOutputEOS = false
        numOutputFrame = 0
        index = 0
        val sTime: Long = stats.curTime
        mediaCodec = createCodec(codecName, format)
        if (mediaCodec == null) {
            return -2
        }
        if (asyncMode) {
            mediaCodec!!.setCallback(object : MediaCodec.Callback() {
                override fun onInputBufferAvailable(
                    mediaCodec: MediaCodec, inputBufferId: Int
                ) {
                    try {
                        stats.addInputTime()
                        onInputAvailable(inputBufferId, mediaCodec)
                    } catch (exception: Exception) {
                        logger.error(
                            "$TAG ", exception
                        )
                    }
                }

                override fun onOutputBufferAvailable(
                    mediaCodec: MediaCodec,
                    outputBufferId: Int, bufferInfo: MediaCodec.BufferInfo
                ) {
                    stats.addOutputTime()
                    onOutputAvailable(mediaCodec, outputBufferId, bufferInfo)
                    if (sawOutputEOS) {
                        lock.withLock { condition.signal() }
                    }
                }

                override fun onOutputFormatChanged(
                    mediaCodec: MediaCodec, format: MediaFormat
                ) {
                    logger.info(
                        "$TAG Output format changed. Format: $format"
                    )
                }

                override fun onError(
                    mediaCodec: MediaCodec, codecException: CodecException
                ) {
                    signalledError = true
                    logger.error(
                        "$TAG Codec Error", codecException
                    )
                    lock.withLock {
                        condition.signal()
                    }
                }
            })
        }
        val isEncoder = 0
        if (DEBUG) {
            logger.debug(
                "$TAG Media Format : $format"
            )
        }
        mediaCodec!!.configure(format, null, null, isEncoder)
        mediaCodec!!.start()
        logger.info("$TAG Codec started ")
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
                logger.error("$TAG Interrupted exception", interruptedException)
            }
        } else {
            while (!sawOutputEOS && !signalledError) {
                /* Queue input data */
                if (!sawInputEOS) {
                    val inputBufferId =
                        mediaCodec!!.dequeueInputBuffer(kQueueDequeueTimeoutUs.toLong())
                    if (inputBufferId < 0 && inputBufferId != MediaCodec.INFO_TRY_AGAIN_LATER) {
                        logger.error(
                            "$TAG MediaCodec.dequeueInputBuffer "
                                    + " returned invalid index : $inputBufferId"
                        )
                        return -1
                    }
                    stats.addInputTime()
                    onInputAvailable(inputBufferId, mediaCodec!!)
                }
                /* Dequeue output data */
                val outputBufferInfo = MediaCodec.BufferInfo()
                val outputBufferId = mediaCodec!!.dequeueOutputBuffer(
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
                    outputBufferId == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                        logger.info(
                            "$TAG Ignoring deprecated flag: INFO_OUTPUT_BUFFERS_CHANGED"
                        )
                    }
                    outputBufferId != MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        logger.error(
                            ("$TAG MediaCodec.dequeueOutputBuffer"
                                    + " returned invalid index $outputBufferId")
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
        this.inputBuffer!!.clear()
        this.inputBufferInfo!!.clear()
        return 0
    }

    /**
     * Stops the codec and releases codec resources.
     */
    fun deInitCodec() {
        val sTime: Long = stats.curTime
        mediaCodec?.let {
            it.stop()
            it.release()
            mediaCodec = null
        }
        val eTime: Long = stats.curTime
        stats.deInitTime = (stats.getTimeDiff(sTime, eTime))
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
        val operation = "decode"
        stats.dumpStatistics(
            inputReference!!, operation, componentName!!, mode!!, durationUs, statsFile
        )
    }

    /**
     * Resets the stats
     */
    fun resetDecoder() {
        stats.reset()
    }

    /**
     * Returns the format of the output buffers
     */
    val format: MediaFormat
        get() = mediaCodec!!.outputFormat

    private fun onInputAvailable(inputBufferId: Int, mediaCodec: MediaCodec) {
        if (inputBufferId >= 0 && !sawInputEOS) {
            val inputCodecBuffer = mediaCodec.getInputBuffer(inputBufferId)
            val bufInfo = inputBufferInfo!![index]
            inputCodecBuffer!!.put(inputBuffer!![index].array())
            index++
            sawInputEOS = (bufInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
            if (sawInputEOS) {
                logger.info("$TAG Saw input EOS")
            }
            stats.addFrameSize(bufInfo.size)
            mediaCodec.queueInputBuffer(
                inputBufferId, bufInfo.offset, bufInfo.size,
                bufInfo.presentationTimeUs, bufInfo.flags
            )
            if (DEBUG) {
                logger.debug(
                    ("$TAG Codec Input: " +
                            "flag = ${bufInfo.flags}  " +
                            "timestamp = ${bufInfo.presentationTimeUs} " +
                            "size = ${bufInfo.size}")
                )
            }
        }
    }

    private fun onOutputAvailable(
        mediaCodec: MediaCodec, outputBufferId: Int, outputBufferInfo: MediaCodec.BufferInfo
    ) {
        if (sawOutputEOS || outputBufferId < 0) {
            return
        }
        numOutputFrame++
        if (DEBUG) {
            logger.debug(
                ("$TAG In OutputBufferAvailable ,"
                        + " output frame number = $numOutputFrame")
            )
        }
        outputStream?.let {
            try {
                val outputBuffer = mediaCodec.getOutputBuffer(outputBufferId)
                val bytesOutput = ByteArray(outputBuffer!!.remaining())
                outputBuffer[bytesOutput]
                it.write(bytesOutput)
            } catch (ioException: IOException) {
                logger.error(
                    "$TAG Error Dumping File: Exception", ioException
                )
            }
        }
        mediaCodec.releaseOutputBuffer(outputBufferId, false)
        sawOutputEOS = (outputBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
        if (sawOutputEOS) {
            logger.info("$TAG Saw output EOS")
        }
    }
}
