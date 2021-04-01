package com.logitech.integration.test.helpers.mode

import com.logitech.service.models.mode.DeviceMode
import com.logitech.service.modeservice.IModeListener
import com.logitech.service.modeservice.ModeServiceManager
import io.mockk.mockk
import org.slf4j.LoggerFactory

class ModeListenerHelper(
    var modeService: ModeServiceManager? = null,
    var listenerMock: IModeListener = mockk(relaxed = true)
) : IModeListener.Stub() {
    val logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun onModeChanged(deviceMode: DeviceMode?) {
        logger.info("inside onModeChanged: $deviceMode")
        listenerMock.onModeChanged(deviceMode)
    }

    override fun onUsbConnection(p0: Boolean) {
        logger.info("inside onUsbConnection: $p0")

    }

    override fun onUsbStableConnection(p0: Boolean) {
        logger.info("inside onUsbStableConnection: $p0")
    }
}