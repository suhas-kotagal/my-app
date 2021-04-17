package com.logitech.integration.test.camera


import android.media.MediaRecorder
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.logitech.integration.test.camera.model.CAMERA
import com.logitech.integration.test.camera.model.CameraRecordOptions
import com.logitech.integration.test.camera.model.CameraSettings
import com.logitech.integration.test.common.VideoSize
import com.logitech.integration.test.common.createVideoFile
import com.logitech.integration.test.common.rules.CameraActivityBlueShellRule
import com.logitech.integration.test.helpers.ptzf.applyMovementAndAssert
import com.logitech.integration.test.helpers.ptzf.centerAndAssert
import com.logitech.integration.test.helpers.ptzf.applyPanMovementAndAssert
import com.logitech.integration.test.helpers.ptzf.applyTiltMovementAndAssert
import com.logitech.integration.test.helpers.ptzf.applyZoomMovementAndAssert

import io.mockk.verify
import logitech.hardware.camera.PtzDevice
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.slf4j.LoggerFactory
import java.io.File


/**
 *  adb shell am instrument -w -r -e class 'com.logitech.integration.test.CameraPtzTestBlueShell' com.logitech.integration.test/androidx.test.runner.AndroidJUnitRunner
 *  #Run with coverage
 *
 *  ./gradlew createDebugCoverageReport
 *  adb shell am instrument -w -r -e coverageFile /data/data/com.logitech.integration.test/coverage.ec -e coverage true -e class 'com.logitech.integration.test.CameraPtzTestBlueShell' com.logitech.integration.test.test/androidx.test.runner.AndroidJUnitRunner
 *  adb shell am instrument -w -r -e coverageFile /data/data/com.logitech.integration.test/coverage.ec -e coverage true -e class 'com.logitech.integration.test.CameraPtzTestBlueShell' com.logitech.integration.test.test/androidx.test.runner.AndroidJUnitRunner
 * START FLAGS IS: cn = ComponentInfo{com.logitech.integration.test.test/androidx.test.runner.AndroidJUnitRunner}, pf = null, flags=0, args=Bundle[{coverageFile=/data/data/com.logitech.integration.test/coverage.ec, coverage=true, class=com.logitech.integration.test.CameraPtzTestBlueShell}], watcher=com.android.commands.am.Instrument$InstrumentationWatcher@9cfdba6, connection=android.app.UiAutomationConnection@ad149e7, userId = -2, abi=null
 */

const val DEFAULT_SPEED = 0f
@RunWith(Parameterized::class)
class CameraPtzTestBlueShell(val fps: Int, val recordVideo: Boolean, val videoSize: VideoSize, val videoEncoding: Int,  val audioEncoding: Int, val videoBitRate: Int, val outputFormat: Int) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(30, true, VideoSize.VID_1920_1080, MediaRecorder.VideoEncoder.H264, MediaRecorder.AudioEncoder.AAC, 10_000_000, MediaRecorder.OutputFormat.MPEG_4),
                arrayOf(30, true, VideoSize.VID_1920_1080, MediaRecorder.VideoEncoder.HEVC, MediaRecorder.AudioEncoder.AAC, 10_000_000, MediaRecorder.OutputFormat.MPEG_4),
                arrayOf(60, true, VideoSize.VID_1920_1080, MediaRecorder.VideoEncoder.H264, MediaRecorder.AudioEncoder.AAC, 10_000_000, MediaRecorder.OutputFormat.MPEG_4),
                arrayOf(60, true, VideoSize.VID_1920_1080, MediaRecorder.VideoEncoder.HEVC, MediaRecorder.AudioEncoder.AAC, 10_000_000, MediaRecorder.OutputFormat.MPEG_4),
                arrayOf(30, true, VideoSize.VID_1920_1080, MediaRecorder.VideoEncoder.H264, MediaRecorder.AudioEncoder.AAC, 10_000_000, MediaRecorder.OutputFormat.WEBM),
                arrayOf(60, true, VideoSize.VID_1920_1080, MediaRecorder.VideoEncoder.HEVC, MediaRecorder.AudioEncoder.AAC, 10_000_000, MediaRecorder.OutputFormat.WEBM)
            )
        }
    }

    val logger = LoggerFactory.getLogger(this.javaClass.name)
    var outputFile : File? = null

    @get:Rule
    val blueShellRule = CameraActivityBlueShellRule()

    fun paramToCameraSettting() : CameraSettings {
        outputFile = if (recordVideo) createVideoFile(InstrumentationRegistry.getInstrumentation().targetContext, "mp4") else null
        return CameraSettings(fps, CAMERA.MAIN_CAMERA.cameraId, videoSize.point, CameraRecordOptions(outputFile, videoEncoding, audioEncoding, videoBitRate, outputFormat))
    }

    @Test
    @MediumTest
    fun cameraTest(){
        val settings = paramToCameraSettting()
        blueShellRule.initCamera(settings)
        blueShellRule.blockingWithPtzDevice({ ptzDevice, ptzServiceHelper ->
            val rangeSet = ptzServiceHelper.getRangeSet()
            logger.info("Range set = {}", rangeSet)
            ptzDevice.centerAndAssert(ptzServiceHelper)
            ptzDevice.applyMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_ABSOLUTE, rangeSet.panRange.min.toFloat(), rangeSet.tiltRange.min.toFloat(), rangeSet.zoomRange.min.toFloat())
            ptzDevice.applyMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_ABSOLUTE, rangeSet.panRange.max.toFloat(), rangeSet.tiltRange.max.toFloat(), rangeSet.zoomRange.max.toFloat())
            verify (atLeast = 1){ blueShellRule.ptzListener.onMotionEvent(any(), any()) }
       })
        blueShellRule.finish()
        val results = blueShellRule.activityScenario?.result
        assertNotNull(results)
        //TODO: futher verfication here, (a) is file present? (b) can we extract the meta data from it and validate it? (c) verify it has an audio track ...
    }

    @Test
    @MediumTest
    fun cameraPanAbsoluteTest() {
        val settings = paramToCameraSettting()
        blueShellRule.initCamera(settings)
        blueShellRule.blockingWithPtzDevice({ ptzDevice, ptzServiceHelper ->
            val rangeSet = ptzServiceHelper.getRangeSet()
            logger.info("Range set = {}", rangeSet)
            ptzDevice.centerAndAssert(ptzServiceHelper)
            ptzDevice.applyPanMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_ABSOLUTE, rangeSet.panRange.min.toFloat(), DEFAULT_SPEED)
            ptzDevice.applyPanMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_ABSOLUTE, rangeSet.panRange.max.toFloat(), DEFAULT_SPEED)
            verify(atLeast = 1) { blueShellRule.ptzListener.onMotionEvent(any(), any()) }
        })
        blueShellRule.finish()

        val results = blueShellRule.activityScenario?.result
        assertNotNull(results)
    }


    @Test
    @MediumTest
    fun cameraPanRelativeTest() {
        val settings = paramToCameraSettting()
        blueShellRule.initCamera(settings)
        blueShellRule.blockingWithPtzDevice({ ptzDevice, ptzServiceHelper ->
            val rangeSet = ptzServiceHelper.getRangeSet()
            logger.info("Range set = {}", rangeSet)
            ptzDevice.centerAndAssert(ptzServiceHelper)
            ptzDevice.applyPanMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_RELATIVE, rangeSet.panRange.min.toFloat(), DEFAULT_SPEED)
            ptzDevice.applyPanMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_RELATIVE, rangeSet.panRange.max.toFloat(), DEFAULT_SPEED)
            verify(atLeast = 1) { blueShellRule.ptzListener.onMotionEvent(any(), any()) }
        })
        blueShellRule.finish()

        val results = blueShellRule.activityScenario?.result
        assertNotNull(results)
    }

   @Test
    @MediumTest
    fun cameraTiltAbsoluteTest() {
        val settings = paramToCameraSettting()
        blueShellRule.initCamera(settings)
        blueShellRule.blockingWithPtzDevice({ ptzDevice, ptzServiceHelper ->
            val rangeSet = ptzServiceHelper.getRangeSet()
            logger.info("Range set = {}", rangeSet)
            ptzDevice.centerAndAssert(ptzServiceHelper)
            ptzDevice.applyTiltMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_ABSOLUTE, rangeSet.tiltRange.min.toFloat(), DEFAULT_SPEED)
            ptzDevice.applyTiltMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_ABSOLUTE, rangeSet.tiltRange.max.toFloat(), DEFAULT_SPEED)
            verify(atLeast = 1) { blueShellRule.ptzListener.onMotionEvent(any(), any()) }
        })
        blueShellRule.finish()

        val results = blueShellRule.activityScenario?.result
        assertNotNull(results)
    }

    @Test
    @MediumTest
    fun cameraTiltRelativeTest() {
        val settings = paramToCameraSettting()
        blueShellRule.initCamera(settings)
        blueShellRule.blockingWithPtzDevice({ ptzDevice, ptzServiceHelper ->
            val rangeSet = ptzServiceHelper.getRangeSet()
            logger.info("Range set = {}", rangeSet)
            ptzDevice.centerAndAssert(ptzServiceHelper)
            ptzDevice.applyTiltMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_RELATIVE, rangeSet.tiltRange.min.toFloat(), DEFAULT_SPEED)
            ptzDevice.applyTiltMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_RELATIVE, rangeSet.tiltRange.max.toFloat(), DEFAULT_SPEED)
            verify(atLeast = 1) { blueShellRule.ptzListener.onMotionEvent(any(), any()) }
        })
        blueShellRule.finish()

        val results = blueShellRule.activityScenario?.result
        assertNotNull(results)
    }


    @Test
    @MediumTest
    fun cameraZoomAbsoluteTest() {
        val settings = paramToCameraSettting()
        blueShellRule.initCamera(settings)
        blueShellRule.blockingWithPtzDevice({ ptzDevice, ptzServiceHelper ->
            val rangeSet = ptzServiceHelper.getRangeSet()
            logger.info("Range set = {}", rangeSet)
            ptzDevice.centerAndAssert(ptzServiceHelper)
            ptzDevice.applyZoomMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_ABSOLUTE, rangeSet.zoomRange.min.toFloat(), DEFAULT_SPEED)
            ptzDevice.applyZoomMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_ABSOLUTE, rangeSet.zoomRange.max.toFloat(), DEFAULT_SPEED)
            verify(atLeast = 1) { blueShellRule.ptzListener.onMotionEvent(any(), any()) }
        })
        blueShellRule.finish()

        val results = blueShellRule.activityScenario?.result
        assertNotNull(results)
    }

    @Test
    @MediumTest
    fun cameraZoomRelativeTest() {
        val settings = paramToCameraSettting()
        blueShellRule.initCamera(settings)
        blueShellRule.blockingWithPtzDevice({ ptzDevice, ptzServiceHelper ->
            val rangeSet = ptzServiceHelper.getRangeSet()
            logger.info("Range set = {}", rangeSet)
            ptzDevice.centerAndAssert(ptzServiceHelper)
            ptzDevice.applyZoomMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_RELATIVE, -1.0f, DEFAULT_SPEED)
            ptzDevice.applyZoomMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_RELATIVE, rangeSet.zoomRange.max.toFloat(), DEFAULT_SPEED)
            verify(atLeast = 1) { blueShellRule.ptzListener.onMotionEvent(any(), any()) }
        })
        blueShellRule.finish()

        val results = blueShellRule.activityScenario?.result
        assertNotNull(results)
    }

    @After
    fun cleanup(){
        outputFile?.let{
            if (it.isFile()){
                it.delete()
            }
        }
    }
}
