package com.logitech.integration.test.common.rules

import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.logitech.integration.test.MainActivity
import com.logitech.integration.test.R
import com.logitech.integration.test.camera.model.CameraSettings
import com.logitech.integration.test.common.SLEEP_INTERVAL_MS
import com.logitech.integration.test.common.SLEEP_TIME_MS
import com.logitech.integration.test.helpers.aicv.AicvServiceHelper
import com.logitech.integration.test.helpers.common.pollForResult
import com.logitech.integration.test.helpers.ptzf.PtzServiceHelper
import com.logitech.integration.test.helpers.ptzf.centerAndAssert
import io.mockk.mockk
import junit.framework.Assert.assertTrue
import logitech.hardware.camera.PtzDevice
import logitech.hardware.camera.PtzManager
import org.junit.Assert
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * A test rule for PTZ controls via BlueShell with a render surface. The rule handles the following:
 *  (1) triggers the correct fragement for rendering the camera stream
 *  (2) Always centers the camera to start and end the test
 *  (3) provides and API to allow the caller to block until PTZ action is complete.
 */

class CameraActivityBlueShellRule : TestRule {

    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    private var appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var cameraSettings: CameraSettings
    val ptzListener = mockk<PtzDevice.Listener>(relaxed = true)
    var activityScenario: ActivityScenario<MainActivity>? = null



    fun initCamera(cameraSettings: CameraSettings): ActivityScenario<MainActivity>? {
        this.cameraSettings = cameraSettings
        val intent = Intent(appContext, MainActivity::class.java).putExtra(
            CameraSettings.EXTRA_NAME,
            cameraSettings
        )
        activityScenario = ActivityScenario.launch<MainActivity>(intent)

        activityScenario?.moveToState(Lifecycle.State.RESUMED)
        Espresso.onView(ViewMatchers.withId(R.id.camera_button)).perform(ViewActions.click())

        cameraSettings.cameraRecordOptions?.outputFileName?.let {
            Espresso.onView(ViewMatchers.withId(R.id.capture_button)).perform(ViewActions.click())
        }


        return activityScenario
    }

    fun blockingWithServiceHelper(
        func: (PtzServiceHelper) -> Unit,
        timeout: Long = 100,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): CameraActivityBlueShellRule {
        requireNotNull(activityScenario) { "Call initCamera() before testing PTZF values." }
        val latch = CountDownLatch(1)
        PtzServiceHelper().use { ptzServiceHelper ->
            assertTrue(ptzServiceHelper.init())
            AicvServiceHelper().use { it.disableAicv() }
            func.invoke(ptzServiceHelper)
            ptzServiceHelper.unregister()
            latch.countDown()
        }
        latch.await(timeout, timeUnit)
        return this
    }

    /**
     * Execute the lambda as a blocking call. Closes resources on behalf of the caller
     *  Fails if the camera cannot be connected to
     *  Fails if the camera makes a disconnect callback
     */
    fun blockingWithPtzDevice(
        func: (PtzDevice, PtzServiceHelper) -> Unit,
        timeout: Long = 100,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): CameraActivityBlueShellRule {
        requireNotNull(activityScenario) { "Call initCamera() before testing PTZ values." }

        val latch = CountDownLatch(1)
        val handlerThread = HandlerThread("ptz-thread").apply { start() }
        val handler = Handler(handlerThread.looper)

        PtzManager.getInstance(appContext) { ptzManager ->

            //logger.info("KONGINTEGRATION cameras: ${ptzManager.ptzCameras.size}")
            ptzManager.open(ptzManager.ptzCameras.first(), object : PtzManager.StateCallback() {
                override fun onOpened(ptzDevice: PtzDevice) {

                    ptzDevice.registerListener(ptzListener, handler)
                    PtzServiceHelper().use { ptzServiceHelper ->
                        AicvServiceHelper().use { it.disableAicv() }

                        func.invoke(ptzDevice, ptzServiceHelper)
                        latch.countDown()

                        ptzDevice.centerAndAssert(ptzServiceHelper)
                        ptzServiceHelper.unregister()
                        ptzDevice.close()
                    }
                    ptzDevice.unregisterListener(ptzListener)
                }

                override fun onDisconnected(ptzDevice: PtzDevice) =
                    Assert.fail("Camera disconnected ${ptzDevice}")

                override fun onError(ptzDevice: PtzDevice?, errorCode: Int) =
                    Assert.fail("Error opening camera ${ptzDevice} error code = ${errorCode}")
            })
        }
        latch.await(timeout, timeUnit)
        return this
    }

    fun checkFileStatus(outputFile: File?): Boolean {
        return outputFile?.let {
            it.exists() && it.length() > 0
        } ?: false
    }

    fun finish() {
        cameraSettings.cameraRecordOptions?.outputFileName?.let {
            Espresso.onView(ViewMatchers.withId(R.id.capture_button)).perform(ViewActions.click())
        }
    }

    override fun apply(base: Statement, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                return base.evaluate()
            }
        }
    }
}
