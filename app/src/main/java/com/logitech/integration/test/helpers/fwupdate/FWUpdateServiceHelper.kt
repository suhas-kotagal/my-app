package com.logitech.integration.test.helpers.fwupdate

import com.logitech.integration.test.helpers.common.begin
import com.logitech.service.fwupdate.FWUpdateServiceManager
import org.slf4j.LoggerFactory
import java.io.Closeable

class FWUpdateServiceHelper : Closeable {
    val logger = LoggerFactory.getLogger(this.javaClass.name)
    val fwupdateManager: FWUpdateServiceManager by lazy {
        val mgr = FWUpdateServiceManager().begin()
        mgr.registerListener(fwUpdateListener)
        fwUpdateListener.fwupdateService = mgr
        mgr
    }
    val fwUpdateListener = FWUpdateListenerHelper()

    override fun close() {
    }
}

