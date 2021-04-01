package com.logitech.integration.test.helpers.hdmi

import com.logitech.integration.test.helpers.common.begin
import com.logitech.service.hdmiservice.HDMIServiceManager
import com.logitech.service.modeservice.ModeServiceManager
import org.slf4j.LoggerFactory
import java.io.Closeable

class HDMIServiceHelper : Closeable {
    val logger = LoggerFactory.getLogger(this.javaClass.name)
    val hdmiManager: HDMIServiceManager by lazy {
        val mgr = HDMIServiceManager().begin()
        mgr.registerListener(hdmiListener)
        hdmiListener.hdmiService = mgr
        mgr
    }
    val hdmiListener = HDMIListenerHelper()

    override fun close() {
    }
}

