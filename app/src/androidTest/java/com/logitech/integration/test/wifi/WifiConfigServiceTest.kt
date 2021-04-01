package com.logitech.integration.test.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.test.platform.app.InstrumentationRegistry
import com.logitech.integration.test.common.ConfigServiceTest
import com.logitech.integration.test.common.SLEEP_INTERVAL_S
import com.logitech.integration.test.common.SLEEP_MAX_TIMEOUT_S
import com.logitech.integration.test.helpers.common.pollForResult
import com.logitech.syncdatamodel.model.device.DeviceControlResponse
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.TimeUnit


@RunWith(JUnit4::class)
class WifiConfigServiceTest {

    private lateinit var configServiceHelper: ConfigServiceTest
    private lateinit var receiver: Receiver
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    @Before
     fun setUp() {
        configServiceHelper = ConfigServiceTest()
        receiver = Receiver()
        val filter = IntentFilter()
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        context.registerReceiver(receiver, filter)
    }

    @After
    fun tearDownTestSuite() {
        context.unregisterReceiver(receiver)
    }

    @Test
    fun testWifiConnected() {
            if (wifiManager.isWifiEnabled) {
                configServiceHelper.disableWifi()
            }
            Assert.assertEquals(DeviceControlResponse.SUCCESS, configServiceHelper.enableWifi())
            Assert.assertEquals(
                WifiManager.WIFI_STATE_ENABLED,
                receiver.getResult(WifiManager.WIFI_STATE_ENABLED)
            )
    }

   @Test
    fun testWifiDisconnected() {
           if (!wifiManager.isWifiEnabled) {
               configServiceHelper.enableWifi()
           }
           Assert.assertEquals(DeviceControlResponse.SUCCESS, configServiceHelper.disableWifi())
           Assert.assertEquals(
               WifiManager.WIFI_STATE_DISABLED,
               receiver.getResult(WifiManager.WIFI_STATE_DISABLED)
           )

    }

    private class Receiver : BroadcastReceiver() {
        var wifiCurrentState : Int = 0
        override fun onReceive(context: Context, intent: Intent) {
            var wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)
            when (wifiStateExtra) {
                WifiManager.WIFI_STATE_ENABLED -> {

                    wifiCurrentState = wifiStateExtra

                }
                WifiManager.WIFI_STATE_DISABLED -> {

                    wifiCurrentState = wifiStateExtra
                }
            }
        }

        fun getResult(wifiState : Int): Any? {
            pollForResult(
                SLEEP_MAX_TIMEOUT_S,
                SLEEP_INTERVAL_S,
                TimeUnit.SECONDS
            ) {
                wifiState == wifiCurrentState

            }
            return wifiState
        }
    }
}
