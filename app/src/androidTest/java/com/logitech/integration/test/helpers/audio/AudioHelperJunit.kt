package com.logitech.integration.test.helpers.audio

import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.view.KeyEvent.KEYCODE_VOLUME_DOWN
import android.view.KeyEvent.KEYCODE_VOLUME_UP
import androidx.test.platform.app.InstrumentationRegistry
import com.logitech.integration.test.helpers.common.pollForResult
import com.logitech.integration.test.common.MAX_VOLUME
import com.logitech.integration.test.common.SKIP_ASSERT_EQUALS
import com.logitech.integration.test.common.SLEEP_INTERVAL_MS
import com.logitech.integration.test.common.SLEEP_TIME_MS
import com.logitech.integration.test.helpers.common.sendInputEvent
import com.logitech.service.models.audio.OptionalResult
import com.logitech.service.models.audio.Topology
import org.junit.Assert
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private var appContext = InstrumentationRegistry.getInstrumentation().targetContext

private fun assertOptionalResult(
    optionalResult: OptionalResult,
    expectedResult: Int,
    resultZero: Boolean
) {
    if (resultZero) {
        Assert.assertTrue(optionalResult.status)
        Assert.assertNotNull(optionalResult.result)
        if (expectedResult != SKIP_ASSERT_EQUALS) Assert.assertEquals(
            expectedResult,
            optionalResult.result
        )
    } else {
        Assert.assertTrue(optionalResult.status)
        Assert.assertTrue(optionalResult.result != 0)
    }
}

fun setGetAudioProcessing(audioServiceHelper: AudioServiceHelper) {
    var audioProcessing = audioServiceHelper.audioManager.setAudioProcessing(true)
    Assert.assertTrue(audioProcessing)

    var result: OptionalResult = audioServiceHelper.audioManager.audioProcessing
    assertOptionalResult(result, 0, false)

    audioProcessing = audioServiceHelper.audioManager.setAudioProcessing(false)
    Assert.assertTrue(audioProcessing)

    result = audioServiceHelper.audioManager.audioProcessing
    assertOptionalResult(result, 0, true)
}

fun sendVolumeUpKeyEvents(audioServiceHelper: AudioServiceHelper) {
    val audioManager = appContext.getSystemService(AUDIO_SERVICE) as AudioManager
    val beforeVolumeUp = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 10
    if (beforeVolumeUp >= MAX_VOLUME) sendVolumeDownKeyEvents(audioServiceHelper)

    sendInputEvent(appContext, KEYCODE_VOLUME_UP)
    pollForResult(SLEEP_TIME_MS, SLEEP_INTERVAL_MS, TimeUnit.MILLISECONDS) {
        (audioServiceHelper.audioManager.speakerLevelPercentage.result) == (beforeVolumeUp + 10)
    }
    Assert.assertEquals(
        beforeVolumeUp + 10,
        audioServiceHelper.audioManager.speakerLevelPercentage.result
    )
}

fun sendVolumeDownKeyEvents(audioServiceHelper: AudioServiceHelper) {
    val audioManager = appContext.getSystemService(AUDIO_SERVICE) as AudioManager
    val beforeVolumeDown = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 10

    sendInputEvent(appContext, KEYCODE_VOLUME_DOWN)
    pollForResult(SLEEP_TIME_MS, SLEEP_INTERVAL_MS, TimeUnit.MILLISECONDS) {
        (audioServiceHelper.audioManager.speakerLevelPercentage.result) == (beforeVolumeDown - 10)
    }
    Assert.assertEquals(
        beforeVolumeDown - 10,
        audioServiceHelper.audioManager.speakerLevelPercentage.result
    )
    if (beforeVolumeDown == MAX_VOLUME) sendVolumeUpKeyEvents(audioServiceHelper)
}

fun hardwareTopology(audioServiceHelper: AudioServiceHelper) {
    var result: OptionalResult = audioServiceHelper.audioManager.topology
    var topo: ArrayList<Topology> = result.topologyData
    Assert.assertNotNull(topo)
    assertOptionalResult(result, SKIP_ASSERT_EQUALS, true)
}

fun micAudioControls(audioServiceHelper: AudioServiceHelper) {
    val number: Int = Random.nextInt(1, 10) * 10
    audioServiceHelper.audioManager.setMicMute(true)
    var result: OptionalResult = audioServiceHelper.audioManager.micMute
    assertOptionalResult(result, 0, false)

    audioServiceHelper.audioManager.setMicMute(false)
    result = audioServiceHelper.audioManager.micMute
    assertOptionalResult(result, 0, true)

    result = audioServiceHelper.audioManager.micGainCapability
    assertOptionalResult(result, 1, true)

    audioServiceHelper.audioManager.setMicLevelPercentage(number)
    result = audioServiceHelper.audioManager.micLevelPercentage
    assertOptionalResult(result, number, false)
}

fun speakerAudioControls(audioServiceHelper: AudioServiceHelper) {
    val number: Int = Random.nextInt(1, 10) * 10
    audioServiceHelper.audioManager.setSpeakerMute(true)
    var result: OptionalResult = audioServiceHelper.audioManager.speakerMute
    assertOptionalResult(result, 0, false)

    audioServiceHelper.audioManager.setSpeakerMute(false)
    result = audioServiceHelper.audioManager.speakerMute
    assertOptionalResult(result, 0, true)

    result = audioServiceHelper.audioManager.speakerGainCapability
    assertOptionalResult(result, 1, true)

    audioServiceHelper.audioManager.setSpeakerLevelPercentage(number)
    result = audioServiceHelper.audioManager.speakerLevelPercentage
    assertOptionalResult(result, number, true)
    setSpeakerLevelBack(audioServiceHelper)
}

fun setSpeakerLevelBack(audioServiceHelper: AudioServiceHelper) {
    val audioManager = appContext.getSystemService(AUDIO_SERVICE) as AudioManager
    audioServiceHelper.audioManager.setSpeakerLevelPercentage(
        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 10
    )
}

fun sendMotionStateTest(audioServiceHelper: AudioServiceHelper) {
    for (i in 0..3) {
        Assert.assertTrue(audioServiceHelper.audioManager.setMotorState(1 shl i))
        var result: OptionalResult = audioServiceHelper.audioManager.motorState
        assertOptionalResult(result, 1 shl i, true)
    }
}

fun setAudioDeviceMode(audioServiceHelper: AudioServiceHelper, setDeviceMode: Boolean): Boolean {
    var deviceMode = audioServiceHelper.audioManager.deviceMode.result
    if (setDeviceMode != deviceMode.toBoolean()) {
        audioServiceHelper.audioManager.setDeviceMode(setDeviceMode)
        when (setDeviceMode) {
            true -> Assert.assertEquals(1, audioServiceHelper.audioManager.deviceMode.result)
            false -> Assert.assertEquals(0, audioServiceHelper.audioManager.deviceMode.result)
        }
    }
    return deviceMode.toBoolean()
}

fun Int.toBoolean() = this == 1
