package com.logitech.integration.test.helpers.mode

import com.logitech.integration.test.common.VERIFY_MAX_TIMEOUT_MS
import com.logitech.service.models.mode.DeviceMode
import io.mockk.verify
import org.junit.Assert

fun changeDeviceModes(setDeviceMode: DeviceMode): DeviceMode {
    var deviceMode: DeviceMode
    ModeServiceHelper().use { modeServiceHelper ->
        deviceMode = modeServiceHelper.modeManager.deviceMode
        if (deviceMode != setDeviceMode) {
            modeServiceHelper.modeManager.forceDeviceMode = setDeviceMode
            verify(timeout = VERIFY_MAX_TIMEOUT_MS) {
                modeServiceHelper.modeListener.listenerMock.onModeChanged(setDeviceMode)
            }
            Assert.assertEquals(modeServiceHelper.modeManager.deviceMode, setDeviceMode)
        }
    }
    return deviceMode
}
