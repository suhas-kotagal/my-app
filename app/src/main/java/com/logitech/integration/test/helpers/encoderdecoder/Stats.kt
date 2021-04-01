package com.logitech.integration.test.helpers.encoderdecoder

import android.util.Log
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

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
/**
 * Measures Performance.
 */

private const val TAG = "Stats"

class Stats {
    var initTime: Long = 0
    var deInitTime: Long = 0
    private var startTimeNs: Long = 0
    private val frameSizes = mutableListOf<Int>()
    private val inputTimer = mutableListOf<Long>()
    private val outputTimer = mutableListOf<Long>()
    val logger = LoggerFactory.getLogger(this.javaClass.name)

    val curTime: Long
        get() = System.nanoTime()

    fun setStartTime() {
        startTimeNs = System.nanoTime()
    }

    fun addFrameSize(size: Int) {
        frameSizes.add(size)
    }

    fun addInputTime() {
        inputTimer.add(System.nanoTime())
    }

    fun addOutputTime() {
        outputTimer.add(System.nanoTime())
    }

    fun reset() {
        if (frameSizes.size != 0) {
            frameSizes.clear()
        }
        if (inputTimer.size != 0) {
            inputTimer.clear()
        }
        if (outputTimer.size != 0) {
            outputTimer.clear()
        }
    }

    fun getTimeDiff(sTime: Long, eTime: Long): Long {
        return eTime - sTime
    }

    private val totalTime: Long
        private get() {
            if (outputTimer.size == 0) {
                return -1
            }
            val lastTime = outputTimer[outputTimer.size - 1]
            return lastTime - startTimeNs
        }
    private val totalSize: Long
        private get() {
            var totalSize: Long = 0
            for (size in frameSizes) {
                totalSize += size
            }
            return totalSize
        }

    /**
     * Writes the stats header to a file
     *
     *
     * \param statsFile    file where the stats data is to be written
     */
    @Throws(IOException::class)
    fun writeStatsHeader(statsFile: File?): Boolean {
        val out = FileOutputStream(statsFile, true)
        statsFile?.let {
            if (!statsFile.exists()) return false
        }
        val statsHeader = """
             currentTime, fileName, operation, componentName, NDK/SDK, sync/async, setupTime, destroyTime, minimumTime, maximumTime, averageTime, timeToProcess1SecContent, totalBytesProcessedPerSec, timeToFirstFrame, totalSizeInBytes, totalTime
             
             """.trimIndent()
        out.write(statsHeader.toByteArray())
        out.close()
        return true
    }

    /**
     * Dumps the stats of the operation for a given input media.
     *
     *
     * \param inputReference input media
     * \param operation      describes the operation performed on the input media
     * (i.e. extract/mux/decode/encode)
     * \param componentName  name of the codec/muxFormat/mime
     * \param mode           the operating mode: sync/async.
     * \param durationUs     is a duration of the input media in microseconds.
     * \param statsFile      the file where the stats data is to be written.
     */
    @Throws(IOException::class)
    fun dumpStatistics(
        inputReference: String, operation: String, componentName: String,
        mode: String, durationUs: Long, statsFile: File?
    ) {
        if (outputTimer.size == 0) {
            logger.error("$TAG No output produced")
            return
        }
        val totalTimeTakenNs = totalTime
        val timeTakenPerSec = totalTimeTakenNs * 1000000 / durationUs
        val timeToFirstFrameNs = outputTimer[0] - startTimeNs
        val size = totalSize
        // get min and max output intervals.
        var intervalNs: Long
        var minTimeTakenNs = Long.MAX_VALUE
        var maxTimeTakenNs: Long = 0
        var prevIntervalNs = startTimeNs
        for (idx in 0 until outputTimer.size - 1) {
            intervalNs = outputTimer[idx] - prevIntervalNs
            prevIntervalNs = outputTimer[idx]
            if (minTimeTakenNs > intervalNs) {
                minTimeTakenNs = intervalNs
            } else if (maxTimeTakenNs < intervalNs) {
                maxTimeTakenNs = intervalNs
            }
        }

        // Write the stats row data to file
        var rowData = ""
        rowData += System.nanoTime().toString() + ", "
        rowData += "$inputReference, "
        rowData += "$operation, "
        rowData += "$componentName, "
        rowData += "SDK, "
        rowData += "$mode, "
        rowData += "$initTime, "
        rowData += "$deInitTime, "
        rowData += "$minTimeTakenNs, "
        rowData += "$maxTimeTakenNs, "
        rowData += "${(totalTimeTakenNs / outputTimer.size)}, "
        rowData += "$timeTakenPerSec, "
        rowData += "${(size * 1000000000 / totalTimeTakenNs)}, "
        rowData += "$timeToFirstFrameNs, "
        rowData += "$size, "
        rowData += """
             $totalTimeTakenNs
             
             """.trimIndent()
        val out = FileOutputStream(statsFile, true)
        statsFile?.let {
            assert(statsFile.exists()) { "Failed to open the stats file for writing!" }
        }

        out.write(rowData.toByteArray())
        out.close()
    }
}
