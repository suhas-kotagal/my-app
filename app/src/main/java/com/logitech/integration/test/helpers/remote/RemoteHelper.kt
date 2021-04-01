package com.logitech.integration.test.helpers.remote

import com.logitech.service.models.remote.PresetType
import com.logitech.service.models.remote.RemoteMode
import com.logitech.service.remoteservice.IRemoteListener
import com.logitech.service.remoteservice.RemoteServiceManager
import io.mockk.mockk
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean


class RemoteListenerHelper(
    var remoteManager: RemoteServiceManager? = null,
    var listenerMock: IRemoteListener = mockk(relaxed = true)
) :
    IRemoteListener.Stub() {
    val logger = LoggerFactory.getLogger(this.javaClass.name)
    val isRegistered = AtomicBoolean(true)

    @Synchronized
    override fun onModeChanged(remoteMode: RemoteMode?) {
        logger.info("inside Remote onMode change: $remoteMode")
        listenerMock.onModeChanged(remoteMode)
    }

    override fun onPresetSaved(presetType: PresetType?) {
        logger.info("inside Remote presetType saved: $presetType")
    }

    override fun onHomeButtonPressed() {
        logger.info("inside Remote onHome button pressed")
    }
}