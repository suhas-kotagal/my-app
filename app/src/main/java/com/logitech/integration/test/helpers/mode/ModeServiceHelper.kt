package com.logitech.integration.test.helpers.mode

import com.logitech.integration.test.helpers.common.begin
import com.logitech.service.modeservice.ModeServiceManager
import org.slf4j.LoggerFactory
import java.io.Closeable

class ModeServiceHelper : Closeable {
    val logger = LoggerFactory.getLogger(this.javaClass.name)
    val modeManager: ModeServiceManager by lazy {
        val mgr = ModeServiceManager().begin()
        mgr.registerListener(modeListener)
        modeListener.modeService = mgr
        mgr
    }
    val modeListener = ModeListenerHelper()

    override fun close() {
    }
}

