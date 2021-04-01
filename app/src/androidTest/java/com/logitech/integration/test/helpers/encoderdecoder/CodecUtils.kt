package com.logitech.integration.test.helpers.encoderdecoder

import android.media.MediaCodecList
import android.os.Build


object CodecUtils {
    /**
     * Queries the MediaCodecList and returns codec names of supported codecs.
     *
     * @param mimeType  Mime type of input
     * @param isEncoder Specifies encoder or decoder
     * @return ArrayList of codec names
     */
    fun selectCodecs(mimeType: String?, isEncoder: Boolean): MutableList<String> {
        val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        val codecInfos = codecList.codecInfos
        val supportedCodecs = mutableListOf<String>()

        codecInfos
            .filterNot { isEncoder != it.isEncoder }
            .filterNot { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && it.isAlias }
            .forEach { codecInfo ->
                codecInfo.supportedTypes
                    .filter { it.equals(mimeType, ignoreCase = true) }
                    .forEach { type ->
                        supportedCodecs.add(codecInfo.name)
                    }
            }
        return supportedCodecs
    }
}
