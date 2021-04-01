package com.logitech.integration.test.common.rules

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.logitech.integration.test.MainActivity
import com.logitech.integration.test.helpers.audio.AudioServiceHelper
import com.logitech.integration.test.helpers.audio.setAudioDeviceMode
import com.logitech.integration.test.helpers.mode.changeDeviceModes
import com.logitech.service.models.mode.DeviceMode
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class AudioRule : TestRule {
    var activityScenario: ActivityScenario<MainActivity>? = null
    private var appContext = InstrumentationRegistry.getInstrumentation().targetContext

    fun executingAudioTests(func: (AudioServiceHelper) -> Unit, hostMode: Boolean): AudioRule {
        var currentDeviceMode: DeviceMode
        var currentAudioDeviceMode: Boolean
        AudioServiceHelper().use { audioServiceHelper ->
            if (hostMode) {
                currentDeviceMode = changeDeviceModes(DeviceMode.HOST)
                initActivity()
                func.invoke(audioServiceHelper)
                changeDeviceModes(currentDeviceMode)
            } else {
                currentAudioDeviceMode = setAudioDeviceMode(audioServiceHelper, false)
                func.invoke(audioServiceHelper)
                setAudioDeviceMode(audioServiceHelper, currentAudioDeviceMode)
            }
        }
        return this
    }

    private fun initActivity() {
        val intent = Intent(appContext, MainActivity::class.java)
        activityScenario = ActivityScenario.launch(intent)
        activityScenario?.moveToState(Lifecycle.State.RESUMED)
    }

    override fun apply(base: Statement, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                return base.evaluate()
            }
        }
    }
}