package com.logitech.integration.test.helpers.fwupdate

import com.logitech.service.fwupdate.FWUpdateServiceManager
import com.logitech.service.fwupdate.IFWUpdateListener
import com.logitech.service.models.update.FWUpdateFlow
import com.logitech.service.models.update.FWUpdateGlobalState
import com.logitech.service.models.update.UpdateState
import org.slf4j.LoggerFactory

class FWUpdateListenerHelper(
    var fwupdateService: FWUpdateServiceManager? = null,
) : IFWUpdateListener.Stub() {
    val logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun onUpdateStarted(p0: FWUpdateFlow?, p1: MutableList<Any?>?) {
        logger.info("inside onUpdateStarted: $p0, $p1")
    }

    override fun onUpdateProgress(
        fwUpdateFlow: FWUpdateFlow?,
        index: Int,
        updateState: UpdateState?,
        progress: Float,
        totalProgress: Int
    ) {
        logger.info("inside onUpdateProgress with flow: $fwUpdateFlow, at $index, with state: $updateState, current progress: $progress, total progress: $totalProgress")
    }

    override fun onUpdateFinished(fwUpdateFlow: FWUpdateFlow?, updateJobs: MutableList<Any?>?) {
        logger.info("inside onUpdateFinished: with flow: $fwUpdateFlow, and updated jobs: $updateJobs.")
    }

    override fun onStateUpdate(fwUpdateFlow: FWUpdateFlow?, state: FWUpdateGlobalState?) {
        logger.info("inside onStateUpdate: with flow: $fwUpdateFlow, and state: $state")
    }

    override fun onDownloadProgress(fwUpdateFlow: FWUpdateFlow?, progress: Float) {
        logger.info("inside onDownloadProgress: with flow: $fwUpdateFlow, and progress: $progress")
    }
}