package com.logitech.integration.test.common.rules

import com.logitech.integration.test.helpers.mode.changeDeviceModes
import com.logitech.integration.test.helpers.remote.RemoteServiceHelper
import com.logitech.service.models.mode.DeviceMode
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RemoteRule : TestRule {
    fun executingRemoteTests(func: (RemoteServiceHelper) -> Unit): RemoteRule {
        var currentDeviceMode: DeviceMode
        RemoteServiceHelper().use { remoteServiceHelper ->
            currentDeviceMode = changeDeviceModes(DeviceMode.HOST)
            func.invoke(remoteServiceHelper)
            changeDeviceModes(currentDeviceMode)
        }
        return this
    }

    override fun apply(base: Statement, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                return base.evaluate()
            }
        }
    }
}