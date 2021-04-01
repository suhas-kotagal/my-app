package com.logitech.integration.test.helpers.encoderdecoder

import android.media.MediaFormat

data class EncoderData(val inputFile: String) {
    var mime = MediaFormat.MIMETYPE_VIDEO_VP8
    var videoBitRate = 600000
    var pixelWidth = 1920
    var pixelHeight = 1080
    var frameInterval = 1
    var profile = -1
    var level = -1
    var sampleRate = -1
    var numChannel = -1

}

data class DecoderData(val inputFile: String) {
    var async = true
}
