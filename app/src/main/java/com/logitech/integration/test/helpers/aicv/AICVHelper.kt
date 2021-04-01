package com.logitech.integration.test.helpers.aicv

import com.logitech.service.aicvservice.AICVServiceManager
import com.logitech.service.aicvservice.IAICVServiceListener
import com.logitech.service.models.aicv.AICVExecutionMode
import com.logitech.service.models.aicv.AICVPersonCounterMode
import io.mockk.mockk
import org.slf4j.LoggerFactory

class AICVListenerHelper(
    var aicvServiceManager: AICVServiceManager? = null,
    var listenerMock: IAICVServiceListener = mockk(relaxed = true)
) : IAICVServiceListener.Stub() {
    val logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun onPersonCounterModeChanged(aicvPersonCounterMode: AICVPersonCounterMode?) {
        logger.info("inside onPersonCounterModeChanged: aicvPersonCounterMode = $aicvPersonCounterMode")
    }

    override fun onPersonAverageCounterUpdated(personCount: Int) {
        logger.info("inside onPersonAverageCounterUpdated: count = $personCount")
    }

    override fun onPersonInstantCounterUpdated(personCount: Int) {
        logger.info("inside onPersonInstantCounterUpdated: count = $personCount")
    }

    override fun onExecutionModeChanged(aicvExecutionMode: AICVExecutionMode?) {
        listenerMock.onExecutionModeChanged(aicvExecutionMode)
        logger.info("inside onExecutionModeChanged: aicvExecutionMode = $aicvExecutionMode")
    }

    override fun onVfRecordingCompleted(result: Int) {
        logger.info("inside onVfRecordingCompleted: result = $result")
    }

}
