package com.logitech.integration.test.audio

import androidx.test.filters.MediumTest
import com.logitech.integration.test.common.rules.AudioRule
import com.logitech.integration.test.helpers.audio.*
import org.junit.Rule
import org.junit.Test


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

    @get:Rule
    val audioRule = AudioRule()

    @Test
    @MediumTest
    fun audioServiceTest() {
        audioRule.executingAudioTests({ audioServiceHelper ->
            sendVolumeUpKeyEvents(audioServiceHelper)
            sendVolumeDownKeyEvents(audioServiceHelper)
        }, true)

        audioRule.executingAudioTests({ audioServiceHelper ->
            setGetAudioProcessing(audioServiceHelper)
        }, false)
    }

    @Test
    @MediumTest
    fun audioControlTest() {
        audioRule.executingAudioTests({ audioServiceHelper ->
            hardwareTopology(audioServiceHelper)
            micAudioControls(audioServiceHelper)
            speakerAudioControls(audioServiceHelper)
            sendMotionStateTest(audioServiceHelper)
        }, false)
    }
}
