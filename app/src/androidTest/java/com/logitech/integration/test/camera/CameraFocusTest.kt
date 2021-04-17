package com.logitech.integration.test.camera
import androidx.lifecycle.ViewModelProvider
import androidx.test.filters.MediumTest
import com.google.common.base.Stopwatch
import com.logitech.integration.test.camera.model.CameraSettings
import com.logitech.integration.test.camera.model.FocusViewModel
import com.logitech.integration.test.common.VideoSize
import com.logitech.integration.test.common.rules.CameraActivityBlueShellRule
import com.logitech.integration.test.helpers.ptzf.PtzfTypeWrapper
import com.logitech.service.models.ptzf.PtzfType
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


const val FOCUS_ITERATIONS = 10

@RunWith(Parameterized::class)
class CameraFocusTest(val fps: Int, val videoSize: VideoSize) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(30, VideoSize.VID_1920_1080)
            )
        }
    }

    @get:Rule
    val blueShellRule = CameraActivityBlueShellRule()

    val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun paramToCameraSettting() : CameraSettings {
        return CameraSettings(fps = fps, videoSize = videoSize.point, autoFocus = false)
    }

    @Test
    @MediumTest
    // Baseline test for uart vs i2c
    fun focusTestWithCamera2Api() {
        val activityScenario = blueShellRule.initCamera(paramToCameraSettting())
        val finishLatch = CountDownLatch(1)
        activityScenario?.onActivity {


            val viewModel = ViewModelProvider(it).get(FocusViewModel::class.java)
            viewModel.autoFocus.value = false

            Thread.sleep(1000)
            blueShellRule.blockingWithServiceHelper({ ptzHelper ->
                val focusRange = ptzHelper.getFocusRange()
                val stopwatch = Stopwatch.createStarted()
                for (times in 0..FOCUS_ITERATIONS) {
                    for (i in focusRange.min..focusRange.max) {
                        viewModel.camera2InputFocusValue.value = i
                    }
                    logger.info("Waiting for value ${focusRange.max}")
                    ptzHelper.ptzListenerHelper.waitForValue(PtzfType.Focus, focusRange.max, { Assert.fail("Timeout waiting for callback")})
                }
                logger.info("Test Done.")
                val elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS)
                val positionChangesSingleIteration = Math.abs(focusRange.max - focusRange.min)
                val positionChangesTotal = positionChangesSingleIteration * FOCUS_ITERATIONS
                val timePerIterationMs = elapsed / FOCUS_ITERATIONS.toFloat()
                logger.info("Result: Test completed ${FOCUS_ITERATIONS} iterations in ${elapsed} ms  (${String.format("%.2f", timePerIterationMs)} ms per iteration)")
                logger.info("Result:     ${positionChangesTotal} total position changes, avg time per position change = ${String.format("%.2f", timePerIterationMs / positionChangesSingleIteration)} ms")

                finishLatch.countDown()

            })

        }
        finishLatch.await(100, TimeUnit.SECONDS)
        blueShellRule.finish()
    }

    @Test
    @MediumTest
    fun focusTestWithKongService(){
        blueShellRule.initCamera(paramToCameraSettting())
        // TODO: measure with benchmark rul
        blueShellRule.blockingWithServiceHelper({ ptzHelper ->
            val focusRange = ptzHelper.getFocusRange()
            val stopwatch = Stopwatch.createStarted()
            for (times in 0..FOCUS_ITERATIONS) {
                for (i in focusRange.min..focusRange.max) {
                    ptzHelper.setAbsolute(PtzfTypeWrapper.FOCUS, i)
                }
                logger.info("Waiting for value ${focusRange.max}")
                ptzHelper.ptzListenerHelper.waitForValue(PtzfType.Focus, focusRange.max)
            }
            val elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS)
            val positionChangesSingleIteration = Math.abs(focusRange.max - focusRange.min)
            val positionChangesTotal = positionChangesSingleIteration * FOCUS_ITERATIONS
            val timePerIterationMs = elapsed / FOCUS_ITERATIONS.toFloat()
            logger.info("Result: Test completed ${FOCUS_ITERATIONS} iterations in ${elapsed} ms  (${String.format("%.2f", timePerIterationMs)} ms per iteration)")
            logger.info("Result:     ${positionChangesTotal} total position changes, avg time per position change = ${String.format("%.2f", timePerIterationMs / positionChangesSingleIteration)} ms")
        })
        blueShellRule.finish()
    }
}
