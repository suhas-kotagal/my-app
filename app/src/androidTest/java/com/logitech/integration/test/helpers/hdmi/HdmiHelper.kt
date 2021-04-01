package com.logitech.integration.test.helpers.hdmi

import com.logitech.service.hdmiservice.HDMIServiceManager
import com.logitech.service.hdmiservice.IHDMIListener
import com.logitech.service.models.hdmi.HDMIStatus
import io.mockk.mockk
import org.slf4j.LoggerFactory

class HDMIListenerHelper(
    var hdmiService: HDMIServiceManager? = null,
    var listenerMock: IHDMIListener = mockk(relaxed = true)
) : IHDMIListener.Stub() {
    val logger = LoggerFactory.getLogger(this.javaClass.name)
    var hdmiIndex: Int = 2
    var hdmiList: MutableList<HDMIStatus> = mutableListOf()

    override fun onHdmiOutChanged(p0: MutableList<HDMIStatus>?) {
        p0?.forEach {
            hdmiList = p0
            logger.info("KONGINTEGRATION inside onHdmiOutChanged - index: ${it.index}, connected: ${it.isConnected}, main: ${it.isMain} systemId: ${it.systemId}")
        }
    }

    override fun onHdmiInChanged(p0: MutableList<HDMIStatus>?) {
        p0?.forEach {
            logger.info("KONGINTEGRATION inside onHdmiInChanged - index: ${it.index}, connected: ${it.isConnected}, main: ${it.isMain} systemId: ${it.systemId}")
        }
    }
}