package com.logitech.integration.test.helpers.audio

import com.logitech.integration.test.helpers.common.begin
import com.logitech.service.audioservice.AudioServiceManager
import java.io.Closeable

class AudioServiceHelper : Closeable {
    val audioManager: AudioServiceManager by lazy {
        val mgr = AudioServiceManager().begin()
        audioListener.isRegistered.set(true)
        mgr.registerListener(audioListener)
        audioListener.audioService = mgr
        mgr
    }
    val audioListener = AudioListenerHelper()

    override fun close() {
    }
}
