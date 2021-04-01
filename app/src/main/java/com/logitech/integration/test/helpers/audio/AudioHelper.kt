package com.logitech.integration.test.helpers.audio

import com.logitech.service.audioservice.AudioServiceManager
import com.logitech.service.audioservice.IAudioServiceListener
import com.logitech.service.models.audio.AudioBoardStatus
import com.logitech.service.models.audio.CallState
import io.mockk.mockk
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

class AudioListenerHelper(
    var audioService: AudioServiceManager? = null,
    var listenerMock: IAudioServiceListener = mockk(relaxed = true)
) : IAudioServiceListener.Stub() {
    val logger = LoggerFactory.getLogger(this.javaClass.name)
    val isRegistered = AtomicBoolean(true)

    override fun onMuteChanged(muteChanged: Boolean) {
        logger.info("inside onMute change: $muteChanged")
        listenerMock.onMuteChanged(muteChanged)
    }

    override fun onCallStateChanged(callState: CallState?) {
        logger.info("inside onCallState change: $callState")
    }

    override fun onMicpodConnectionChanged(p0: Boolean) {
        logger.info("inside onMicpodConnectionChanged: $p0")
    }

    override fun onAudioBoardStatusChanged(p0: AudioBoardStatus?) {
        logger.info("inside onAudioBoardStatusChanged: $p0")
    }
}
