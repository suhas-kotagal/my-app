package com.logitech.integration.test.helpers.common

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.input.InputManager
import android.os.SystemClock
import android.view.InputEvent
import android.view.KeyCharacterMap
import android.view.KeyEvent
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

fun pollForResult(
    timeOutInMilliseconds: Long,
    timeBetweenPoll: Long,
    timeUnit: TimeUnit,
    pollingFunc: () -> Boolean
) {
    var sleepDuration = TimeUnit.MILLISECONDS.convert(timeBetweenPoll, timeUnit)
    for (i in 1..timeOutInMilliseconds step timeBetweenPoll) {
        if (!pollingFunc.invoke()) {
            Thread.sleep(sleepDuration)
        } else break
    }
}

fun sendInputEvent(appContext: Context, keyCode: Int) {
    val inputManager = appContext.getSystemService(Context.INPUT_SERVICE) as InputManager
    val injectInputEvent: Method = InputManager::class.java.getMethod(
        "injectInputEvent",
        InputEvent::class.java,
        Int::class.javaPrimitiveType
    )
    val now = SystemClock.uptimeMillis()
    val eventDown = KeyEvent(
        now, now, KeyEvent.ACTION_DOWN, keyCode, 0, 0,
        KeyCharacterMap.VIRTUAL_KEYBOARD, 0
    )
    val eventUp = KeyEvent(
        now, now, KeyEvent.ACTION_UP, keyCode, 0, 0,
        KeyCharacterMap.VIRTUAL_KEYBOARD, 0
    )
    injectInputEvent.invoke(inputManager, eventDown, 2)
    injectInputEvent.invoke(inputManager, eventUp, 2)
}

class BluetoothReceiver : BroadcastReceiver() {
    var btState = 0
    override fun onReceive(context: Context, intent: Intent) {
        btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
    }
}

