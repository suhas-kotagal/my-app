package com.logitech.integration.test.helpers.config

import android.bluetooth.BluetoothAdapter
import android.content.IntentFilter
import androidx.test.platform.app.InstrumentationRegistry
import com.logitech.integration.test.common.SLEEP_INTERVAL_MS
import com.logitech.integration.test.common.SLEEP_TIME_MS
import com.logitech.integration.test.common.VERIFY_MAX_TIMEOUT_MS
import com.logitech.integration.test.common.VERIFY_TIMEOUT_MS
import com.logitech.integration.test.helpers.aicv.changeAICVModes
import com.logitech.integration.test.helpers.common.BluetoothReceiver
import com.logitech.integration.test.helpers.common.pollForResult
import com.logitech.integration.test.helpers.fwupdate.getFWPeriodicCheck
import com.logitech.syncdatamodel.model.device.DeviceAICVMode
import org.junit.Assert
import java.util.Random
import java.util.concurrent.TimeUnit

private var appContext = InstrumentationRegistry.getInstrumentation().targetContext
private var bluetoothReceiver: BluetoothReceiver? = null

fun bluetoothOnOff(configServiceHelper: ConfigServiceHelper) {
    bluetoothReceiver = BluetoothReceiver()
    val filter = IntentFilter()
    filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
    appContext.registerReceiver(bluetoothReceiver, filter)

    changeBluetoothState(configServiceHelper)
    changeBluetoothState(configServiceHelper)
}

fun changeBluetoothState(configServiceHelper: ConfigServiceHelper) {
    var currentState = BluetoothAdapter.getDefaultAdapter().isEnabled
    if (currentState) {
        configServiceHelper.configManager.disableBluetooth()
        pollForResult(VERIFY_TIMEOUT_MS, SLEEP_INTERVAL_MS, TimeUnit.MILLISECONDS) {
            (bluetoothReceiver?.btState == BluetoothAdapter.STATE_OFF)
        }
        Assert.assertEquals(BluetoothAdapter.STATE_OFF, bluetoothReceiver?.btState)
    } else {
        configServiceHelper.configManager.enableBluetooth()
        pollForResult(VERIFY_MAX_TIMEOUT_MS, SLEEP_TIME_MS, TimeUnit.MILLISECONDS) {
            (bluetoothReceiver?.btState == BluetoothAdapter.STATE_ON)
        }
        Assert.assertEquals(BluetoothAdapter.STATE_ON, bluetoothReceiver?.btState)
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

fun fwPeriodicCheck(configServiceHelper: ConfigServiceHelper) {
    val random = Random()
    val periodicCheck: Int = random.nextInt(1000 - 180) + 180
    configServiceHelper.configManager.setPeriodicCheckPeriod(periodicCheck)
    var periodicCheckPeriod: Int = getFWPeriodicCheck()
    Assert.assertEquals(periodicCheck, periodicCheckPeriod)
}

