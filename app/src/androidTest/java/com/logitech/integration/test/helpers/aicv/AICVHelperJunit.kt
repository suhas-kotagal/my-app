package com.logitech.integration.test.helpers.aicv

import com.logitech.integration.test.common.VERIFY_TIMEOUT_MS
import com.logitech.service.models.aicv.AICVExecutionMode
import com.logitech.syncdatamodel.model.device.DeviceAICVMode
import io.mockk.verify
import org.junit.Assert


fun changeAICVModes(deviceAICVMode: DeviceAICVMode) {
    var modeMap = mapOf(
    DeviceAICVMode.AT_CALL_STARTUP to AICVExecutionMode.MANUAL,
    DeviceAICVMode.DYNAMIC to AICVExecutionMode.ROOM_WATCH,
    DeviceAICVMode.DISABLED to AICVExecutionMode.DISABLED
    )

    var aicvExecutionModeMode: AICVExecutionMode
    var changeAICVExecutionMode = modeMap[deviceAICVMode]
    AicvServiceHelper().use { aicvServiceHelper ->
        aicvExecutionModeMode = aicvServiceHelper.aicvManager.executionMode

        if (modeMap[deviceAICVMode] != aicvExecutionModeMode) {
            aicvServiceHelper.aicvManager.executionMode = changeAICVExecutionMode
            verify(timeout = VERIFY_TIMEOUT_MS) {
                aicvServiceHelper.aicvListenerHelper.listenerMock.onExecutionModeChanged(
                    changeAICVExecutionMode
                )
            }
            Assert.assertEquals(
                aicvServiceHelper.aicvManager.executionMode,
                modeMap[deviceAICVMode]
            )
        }
    }
}
