package com.logitech.integration.test.audio

import android.util.Log
import androidx.test.filters.MediumTest
import com.logitech.integration.test.common.rules.AudioRule
import com.logitech.integration.test.helpers.audio.*
import org.junit.Rule
import org.junit.Test
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import com.logitech.integration.test.helpers.encoderdecoder.loopDecode
import com.logitech.integration.test.helpers.encoderdecoder.loopEncode

/**
 *  adb shell am instrument -w -r -e class 'com.logitech.integration.test.audio.AudioTest' com.logitech.integration.test/androidx.test.runner.AndroidJUnitRunner
 *  #Run with coverage
 *
 *  ./gradlew createDebugCoverageReport
 *  adb shell am instrument -w -r -e coverageFile /data/data/com.logitech.integration.test/coverage.ec -e coverage true -e class 'com.logitech.integration.test.audio.AudioTest' com.logitech.integration.test.test/androidx.test.runner.AndroidJUnitRunner
 *  adb shell am instrument -w -r -e coverageFile /data/data/com.logitech.integration.test/coverage.ec -e coverage true -e class 'com.logitech.integration.test.audio.AudioTest' com.logitech.integration.test.test/androidx.test.runner.AndroidJUnitRunner
 * START FLAGS IS: cn = ComponentInfo{com.logitech.integration.test.test/androidx.test.runner.AndroidJUnitRunner}, pf = null, flags=0, args=Bundle[{coverageFile=/data/data/com.logitech.integration.test/coverage.ec, coverage=true, class=com.logitech.integration.test.audio.AudioTest}], watcher=com.android.commands.am.Instrument$InstrumentationWatcher@9cfdba6, connection=android.app.UiAutomationConnection@ad149e7, userId = -2, abi=null
 */


class AudioTest() {
    /*@get:Rule
    val benchmarkRule = BenchmarkRule()
    */
    @get:Rule
    val audioRule = AudioRule()

/*    @Test
    @MediumTest
    fun audioServiceTest() {
*//*        audioRule.executingAudioTests({ audioServiceHelper ->
            sendVolumeUpKeyEvents(audioServiceHelper)
            sendVolumeDownKeyEvents(audioServiceHelper)
        }, true)*//*

        audioRule.executingAudioTests({ audioServiceHelper ->
            Log.d("KONGINTEGRATION" , "starting audio tests")
            setGetAudioProcessing(audioServiceHelper)
            Log.d("KONGINTEGRATION" , "completed audio tests")
        }, false)
    }*/



    @Test
    @MediumTest
    fun audioControlTest1() {
        //benchmarkRule.measureRepeated {
            audioRule.executingAudioTests({ audioServiceHelper ->
                hardwareTopology(audioServiceHelper)

               // speakerAudioControls(audioServiceHelper)
               // sendMotionStateTest(audioServiceHelper)
            }, false)
        //}
    }
    @Test
    @MediumTest
    fun audioControlTest2() {
        //benchmarkRule.measureRepeated {
            audioRule.executingAudioTests({ audioServiceHelper ->

                micAudioControls(audioServiceHelper)
               // speakerAudioControls(audioServiceHelper)
               // sendMotionStateTest(audioServiceHelper)
            }, false)
        //}
    }
    @Test
    @MediumTest
    fun audioControlTest3() {
        //benchmarkRule.measureRepeated {
            audioRule.executingAudioTests({ audioServiceHelper ->

               speakerAudioControls(audioServiceHelper)
               // sendMotionStateTest(audioServiceHelper)
            }, false)
        //}
    }
}

