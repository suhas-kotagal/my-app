package com.logitech.integration.test.helpers.aicv

import com.logitech.integration.test.helpers.common.begin
import com.logitech.integration.test.helpers.common.end
import com.logitech.service.aicvservice.AICVServiceManager
import com.logitech.service.models.aicv.AICVExecutionMode
import com.logitech.service.models.aicv.AICVPersonCounterMode
import java.io.Closeable

class AicvServiceHelper : Closeable {
    val aicvManager: AICVServiceManager by lazy {
        val mgr = AICVServiceManager().begin()
        mgr.registerListener(aicvListenerHelper)
        mgr
    }

    var aicvListenerHelper = AICVListenerHelper()
    fun disableAicv(){
        aicvManager.executionMode = AICVExecutionMode.DISABLED
        aicvManager.personCounterMode = AICVPersonCounterMode.DISABLED
    }

    override fun close() = aicvManager.end()
}