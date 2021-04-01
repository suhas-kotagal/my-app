/*package com.logitech.integration.test.helpers.config

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.logitech.integration.test.common.SLEEP_INTERVAL_MS
import com.logitech.integration.test.common.SLEEP_TIME_MS
import com.logitech.integration.test.common.VERIFY_MAX_TIMEOUT_MS
import com.logitech.integration.test.common.VERIFY_TIMEOUT_MS
import com.logitech.integration.test.helpers.aicv.changeAICVModes
import com.logitech.integration.test.helpers.audio.AudioServiceHelper
import com.logitech.integration.test.helpers.common.pollForResult
import com.logitech.integration.test.helpers.fwupdate.getFWPeriodicCheck
import com.logitech.syncdatamodel.model.device.DeviceAICVMode
import com.logitech.syncdatamodel.model.device.UpdateSchedule
import com.logitech.syncdatamodel.model.device.UpdateSchedule.TimeWindow
import org.junit.Assert
import java.util.*
import java.util.concurrent.TimeUnit

private var appContext = InstrumentationRegistry.getInstrumentation().targetContext
private var receiver: Receiver? = null
var btState = 0

fun bluetoothONOFF(configServiceHelper: ConfigServiceHelper) {
    receiver = Receiver()
    val filter = IntentFilter()
    filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
    appContext.registerReceiver(receiver, filter)

    changeBluetoothState(configServiceHelper)
    changeBluetoothState(configServiceHelper)
}

fun changeBluetoothState(configServiceHelper: ConfigServiceHelper) {
    var currentState = BluetoothAdapter.getDefaultAdapter().isEnabled
    if (currentState) {
        configServiceHelper.configManager.disableBluetooth()
        pollForResult(VERIFY_TIMEOUT_MS, SLEEP_INTERVAL_MS, TimeUnit.MILLISECONDS) {
            (btState == BluetoothAdapter.STATE_OFF)
        }
        Assert.assertEquals(BluetoothAdapter.STATE_OFF, btState)
    } else {
        configServiceHelper.configManager.enableBluetooth()
        pollForResult(VERIFY_MAX_TIMEOUT_MS, SLEEP_TIME_MS, TimeUnit.MILLISECONDS) {
            (btState == BluetoothAdapter.STATE_ON)
        }
        Assert.assertEquals(BluetoothAdapter.STATE_ON, btState)
    }
}

fun aicvModesTest(configServiceHelper: ConfigServiceHelper) {
    configServiceHelper.configManager.setAICVMode(DeviceAICVMode.DYNAMIC)
    changeAICVModes(DeviceAICVMode.DYNAMIC)
    configServiceHelper.configManager.setAICVMode(DeviceAICVMode.AT_CALL_STARTUP)
    changeAICVModes(DeviceAICVMode.AT_CALL_STARTUP)
    configServiceHelper.configManager.setAICVMode(DeviceAICVMode.DISABLED)
    changeAICVModes(DeviceAICVMode.DISABLED)
}

fun updateScheduler(configServiceHelper: ConfigServiceHelper){
    val schedule = UpdateSchedule(
        "1.0",
        1610736757000L,
        "10.0.1",
        1610736757000L,
        TimeWindow("2021-03-01T19:30:00", "2021-03-31T21:30:00")
    )
    configServiceHelper.configManager.scheduleUpdate(schedule)

    configServiceHelper.configManager
    val random = Random()
    val periodicCheck: Int = random.nextInt(1000 - 180) + 180
    configServiceHelper.configManager.setPeriodicCheckPeriod(periodicCheck)
    var periodicCheckPeriod: Int = getFWPeriodicCheck()
    Assert.assertEquals(periodicCheck, periodicCheckPeriod)

}

fun audioControlsTest(configServiceHelper: ConfigServiceHelper) {
    val audioManager =
        appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val beforeVolumeUp = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 10
    AudioServiceHelper().use {
        Log.d(
            "KONGINTEGRATION",
            "current vol: ${audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 10} and ${it.audioManager.speakerLevelPercentage.result}" +
                    " and mic: ${it.audioManager.micLevelPercentage.result}"
        )
        Log.d(
            "KONGINTEGRATION",
            "config volup: ${configServiceHelper.configManager.audioVolumeUp(true)}"
        )
        Thread.sleep(500)
        Log.d(
            "KONGINTEGRATION",
            "config volup: ${configServiceHelper.configManager.audioVolumeUp(false)}"
        )

        //sendInputEvent(com.logitech.integration.test.helpers.audio.appContext, KeyEvent.KEYCODE_VOLUME_UP)
        pollForResult(5000, 1000, TimeUnit.MILLISECONDS) {
            (it.audioManager.speakerLevelPercentage.result) == (beforeVolumeUp + 10)
        }

        Log.d(
            "KONGINTEGRATION",
            "after volup: ${audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 10} and ${it.audioManager.speakerLevelPercentage.result}" +
                    "and mic: ${it.audioManager.micLevelPercentage.result}"
        )
    }
    //Log.d("KONGINTEGRATION", "config vol down: ${configServiceHelper.configManager.audioVolumeDown(true) }")
    //Log.d("KONGINTEGRATION", "after vol down: ${audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 10}")
    //Log.d("KONGINTEGRATION", "config vol mute: ${configServiceHelper.configManager.audioMute(true)}")
    //Log.d("KONGINTEGRATION", "after vol mute: ${audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 10}")

}


class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        var action = intent.action

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

            var state =
                intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            when (state) {
                BluetoothAdapter.STATE_OFF -> {
                    btState = BluetoothAdapter.STATE_OFF
                }
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    btState = BluetoothAdapter.STATE_TURNING_OFF
                }
                BluetoothAdapter.STATE_ON -> {
                    btState = BluetoothAdapter.STATE_ON
                }
                BluetoothAdapter.STATE_TURNING_ON -> {
                    btState = BluetoothAdapter.STATE_TURNING_ON
                }
            }
        }
    }
}
*/
