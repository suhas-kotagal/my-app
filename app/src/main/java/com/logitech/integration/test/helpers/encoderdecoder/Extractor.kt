package com.logitech.integration.test.helpers.encoderdecoder

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import java.nio.ByteBuffer

private const val TAG = "Extractor"
private const val kMaxBufSize = 1024 * 1024 * 16

class Extractor {
    val logger = LoggerFactory.getLogger(this.javaClass.name)

    private var extractor: MediaExtractor? = null

    /**
     * Returns the extracted buffer for the input clip
     */
    val frameBuffer: ByteBuffer = ByteBuffer.allocate(kMaxBufSize)

    /**
     * Returns the information of buffer related to sample
     */
    val bufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
    private val stats: Stats = Stats()

    /**
     * Returns the duration of the sample
     */
    var clipDuration: Long = 0

    /**
     * Creates a Media Extractor and sets data source(FileDescriptor)to use
     *
     * @param fileDescriptor FileDescriptor for the file which is to be extracted
     * @return TrackCount of the sample
     * @throws IOException If FileDescriptor is null
     */
    @Throws(IOException::class)
    fun setUpExtractor(fileDescriptor: FileDescriptor?): Int {
        val sTime: Long = stats.curTime
        extractor = MediaExtractor()
        extractor!!.setDataSource(fileDescriptor!!)
        val eTime: Long = stats.curTime
        val timeTaken = stats.getTimeDiff(sTime, eTime)
        stats.initTime = (timeTaken)
        return extractor!!.trackCount
    }

    /**
     * Returns the track format of the specified index
     *
     * @param trackID Index of the track
     * @return Format of the track
     */
    fun getFormat(trackID: Int): MediaFormat {
        return extractor!!.getTrackFormat(trackID)
    }

    /**
     * Retrieve the current sample and store it in the byte buffer
     * Also, sets the information related to extracted sample and store it in buffer info
     *
     * @return Sample size of the extracted sample
     */
    val frameSample: Int
        get() {
            val sampleSize = extractor!!.readSampleData(frameBuffer, 0)
            if (sampleSize < 0) {
                bufferInfo.flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM
                bufferInfo.size = 0
            } else {
                bufferInfo.size = sampleSize
                bufferInfo.offset = 0
                bufferInfo.flags = extractor!!.sampleFlags
                bufferInfo.presentationTimeUs = extractor!!.sampleTime
                extractor!!.advance()
            }
            return sampleSize
        }

    /**
     * Setup the track format and get the duration of the sample
     * Track is selected here for extraction
     *
     * @param trackId Track index to be selected
     * @return 0 for valid track, otherwise -1
     */
    fun selectExtractorTrack(trackId: Int): Int {
        val trackFormat = extractor!!.getTrackFormat(trackId)
        clipDuration = trackFormat.getLong(MediaFormat.KEY_DURATION)
        if (clipDuration < 0) {
            logger.error("$TAG Invalid Clip")
            return -1
        }
        extractor!!.selectTrack(trackId)
        return 0
    }

    /**
     * Unselect the track
     *
     * @param trackId Track Index to be unselected
     */
    fun unselectExtractorTrack(trackId: Int) {
        extractor!!.unselectTrack(trackId)
    }

    /**
     * Free up the resources
     */
    fun deinitExtractor() {
        val beforeRelease: Long = stats.curTime
        extractor!!.release()
        val afterRelease: Long = stats.curTime
        val timeTaken = stats.getTimeDiff(beforeRelease, afterRelease)
        stats.deInitTime = (timeTaken)
    }

    /**
     * Performs extract operation
     *
     * @param currentTrack Track index to be extracted
     * @return Status as 0 if extraction is successful, -1 otherwise
     */
    fun extractSample(currentTrack: Int): Int {
        val status: Int = selectExtractorTrack(currentTrack)
        if (status == -1) {
            logger.error("$TAG Failed to select track")
            return -1
        }
        stats.setStartTime()
        while (true) {
            val readSampleSize = frameSample
            if (readSampleSize <= 0) {
                break
            }
            stats.addOutputTime()
            stats.addFrameSize(readSampleSize)
        }
        unselectExtractorTrack(currentTrack)
        return 0
    }

    /**
     * Write the benchmark logs for the given input file
     *
     * @param inputReference Name of the input file
     * @param mimeType       Mime type of the muxed file
     * @param statsFile      The output file where the stats data is written
     */
    @Throws(IOException::class)
    fun dumpStatistics(inputReference: String?, mimeType: String?, statsFile: File?) {
        val operation = "extract"
        stats.dumpStatistics(
            inputReference!!, operation,
            mimeType!!, "", clipDuration, statsFile
        )
    }
}
