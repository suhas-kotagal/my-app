/*package com.logitech.integration.test.encoderdecoder

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.filters.MediumTest
import com.logitech.integration.test.helpers.encoderdecoder.loopDecode
import com.logitech.integration.test.helpers.encoderdecoder.loopEncode
import org.junit.Rule
import org.junit.Test
import java.io.IOException


class EncoderDecoderTest {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    @MediumTest
    @Throws(IOException::class)
    fun testDecoder() {
        benchmarkRule.measureRepeated {
            //loopEncode()
            loopDecode()
        }
    }
}

*/
