package com.logitech.integration.test.helpers.hdmi

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.test.InstrumentationRegistry
import com.logitech.integration.test.helpers.common.pollForResult
import com.logitech.service.models.hdmi.HDMIStatus
import org.junit.Assert
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

val appContext = InstrumentationRegistry.getInstrumentation().targetContext

class TrackingDisplayListener : DisplayManager.DisplayListener {
    val logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun onDisplayAdded(i: Int) {
        logger.info("KONGINTEGRATION - display manager - Display added with name: $i")

        val displayManager = appContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        logIfAdditionalDisplayFound(displayManager)
    }

    private fun logIfAdditionalDisplayFound(manager: DisplayManager) {
        val displays: Array<Display> = manager.displays
        if (displays.size > 1) {
            for (j in 1 until displays.size) {
                val display: Display = displays[j]
                logger.info("KONGINTEGRATION - display manager - Display found with state: ${display.state}")
            }
        }
    }

    override fun onDisplayRemoved(i: Int) {
        logger.info("KONGINTEGRATION - display manager - Display removed with name: $i")
    }

    override fun onDisplayChanged(i: Int) {
        logger.info("KONGINTEGRATION - display manager - Display changed with name: $i")
    }
}

fun sendCEC(hdmiServiceHelper: HDMIServiceHelper) {
    var connected = false
    var index = 2
    var displayManager =
        appContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
   // displayManager.registerDisplayListener(TrackingDisplayListener(), null)

    var list: List<HDMIStatus> = hdmiServiceHelper.hdmiManager.hdmiOutStatus
    hdmiServiceHelper.logger.info("KONGINTEGRATION - list size: ${list.size}")
    list.forEach {
        hdmiServiceHelper.logger.info(
            "KONGINTEGRATION - HDMI inside sendCEC(), index: " +
                    "${it.index}, connected: ${it.isConnected}, isMain: ${it.isMain}"
        )
        connected = it.isConnected
        if (connected) {
            index = it.index
            hdmiServiceHelper.logger.info("KONGINTEGRATION - index = $index, connected = $connected")
            startCEC(hdmiServiceHelper, index, displayManager)
        }
    }
}

fun startCEC(
    hdmiServiceHelper: HDMIServiceHelper,
    index: Int,
    displayManager: DisplayManager
) {
    var currentCEC = hdmiServiceHelper.hdmiManager.isCECEnabled
    hdmiServiceHelper.logger.info("KONGINTEGRATION - cec enabled: $currentCEC")
    if (!currentCEC) {
        hdmiServiceHelper.hdmiManager.setCEC(true)
        Assert.assertEquals(true, hdmiServiceHelper.hdmiManager.isCECEnabled)

    }

    if (displayManager.getDisplay(index).state == 2) {
        hdmiServiceHelper.logger.info("KONGINTEGRATION - display is ON, sending OFF and ON")
        sendCECOFFAndWait(hdmiServiceHelper, index, displayManager)
        sendCECONAndWait(hdmiServiceHelper, index, displayManager)
    } else {
        hdmiServiceHelper.logger.info("KONGINTEGRATION - display is OFF, sending ON and OFF")
        sendCECONAndWait(hdmiServiceHelper, index, displayManager)
        sendCECOFFAndWait(hdmiServiceHelper, index, displayManager)
    }


    if (!currentCEC) hdmiServiceHelper.hdmiManager.setCEC(currentCEC)
}


fun sendCECOFFAndWait(
    hdmiServiceHelper: HDMIServiceHelper,
    index: Int,
    displayManager: DisplayManager
) {
    hdmiServiceHelper.logger.info(
        "KONGINTEGRATION - sending CEC OFF to another repo again= ${
            hdmiServiceHelper.hdmiManager.sendCecOff(index)
        }"
    )

    hdmiServiceHelper.logger.info(
        "KONGINTEGRATION required state 1, but found: -> ${
            displayManager.getDisplay(
                index
            ).state
        }, name: ${displayManager.getDisplay(index).name}"
    )

    displayManager.displays.forEach {
        //it.displayId and index might be same, so we can assert if that is the same which got STATE_ON as it.state
        hdmiServiceHelper.logger.info("KONGINTEGRATION - displays: name - ${it.name}, id - ${it.displayId}, state -  ${it.state}, isHDR - ${it.isHdr}")
    }

    hdmiServiceHelper.logger.info(
        "KONGINTEGRATION required state 1, but found: ->> ${
            displayManager.getDisplay(
                index
            ).state
        }, name: ${displayManager.getDisplay(index).name}"
    )
    //Assertion value
    //when sent CEC OFF - displayManager.getDisplay(index).state should be STATE_OFF = 1
    hdmiServiceHelper.logger.info("KONGINTEGRATION  - polling now")

    pollForResult(20, 2, TimeUnit.SECONDS) {
        checkDisplayAndAssert(hdmiServiceHelper, index, 1, displayManager)
    }


    hdmiServiceHelper.logger.info("KONGINTEGRATION  - polling completed")
    hdmiServiceHelper.logger.info(
        "KONGINTEGRATION post required state 1, but found: - ${displayManager.getDisplay(index).state}," +
                " name: ${displayManager.getDisplay(index).name}"
    )
    displayManager.displays.forEach {
        //it.displayId and index might be same, so we can assert if that is the same which got STATE_ON as it.state
        hdmiServiceHelper.logger.info("KONGINTEGRATION - post displays: name - ${it.name}, id - ${it.displayId}, state -  ${it.state}, isHDR - ${it.isHdr}")
    }
}

fun checkDisplayAndAssert(
    hdmiServiceHelper: HDMIServiceHelper,
    index: Int,
    expected: Int,
    displayManager: DisplayManager
): Boolean {

    var hdmiExpected: Boolean = false
    displayManager.displays.forEach {
        //it.displayId and index might be same, so we can assert if that is the same which got STATE_ON as it.state
        hdmiServiceHelper.logger.info("KONGINTEGRATION - polling displays: name - ${it.name}, id - ${it.displayId}, state -  ${it.state}, isHDR - ${it.isHdr}")
    }
    hdmiServiceHelper.logger.info("KONGINTEGRATION - response size: ${hdmiServiceHelper.hdmiListener.hdmiList?.size}")


    hdmiServiceHelper.hdmiListener.hdmiList?.let { it ->
        it.forEach() {
            hdmiServiceHelper.logger.info("KONGINTEGRATION - now: ${it.index}, ${it.isConnected}")
            if (it.index == index) {
                hdmiServiceHelper.logger.info("KONGINTEGRATION - index matched: ${it.index} = $index")
                when (expected) {
                    1 -> if (!it.isConnected) {
                        hdmiExpected = true
                        hdmiServiceHelper.logger.info("KONGINTEGRATION - inside 1 -> success")
                        //Assert.assertEquals(false, it.isConnected)
                    }
                    2 -> if (it.isConnected) {
                        hdmiExpected = true
                        hdmiServiceHelper.logger.info("KONGINTEGRATION - inside 2 -> success")
                        //Assert.assertEquals(true, it.isConnected)
                    }
                    else -> {
                        hdmiServiceHelper.logger.info("KONGINTEGRATION - expected default block = $expected")
                        hdmiServiceHelper.logger.info("KONGINTEGRATION - connection status is: ${it.isConnected}")
                    }
                }
            }
        }
    }
    return hdmiExpected

    /*
    displayManager.displays.forEach {
        hdmiServiceHelper.logger.info("KONGINTEGRATION - inside check displays: name - ${it.name}, id - ${it.displayId}, state -  ${it.state}, isHDR - ${it.isHdr}")
    }
    hdmiServiceHelper.logger.info("KONGINTEGRATION - returning ${displayManager.getDisplay(index).state}")
    return displayManager.getDisplay(index).state == expected*/
}

fun sendCECONAndWait(
    hdmiServiceHelper: HDMIServiceHelper,
    index: Int,
    displayManager: DisplayManager
) {
    hdmiServiceHelper.logger.info(
        "KONGINTEGRATION - sending CEC ON to another repo again= ${
            hdmiServiceHelper.hdmiManager.sendCecOn(index)
        }"
    )

    hdmiServiceHelper.logger.info(
        "KONGINTEGRATION required state 2, but found: -> ${
            displayManager.getDisplay(
                index
            ).state
        }, name: ${displayManager.getDisplay(index).name}"
    )


    displayManager.displays.forEach {
        //it.displayId and index might be same, so we can assert if that is the same which got STATE_ON as it.state
        hdmiServiceHelper.logger.info("KONGINTEGRATION - displays: name - ${it.name}, id - ${it.displayId}, state -  ${it.state}, isHDR - ${it.isHdr}")
    }

    hdmiServiceHelper.logger.info(
        "KONGINTEGRATION required state 2, but found: ->> ${
            displayManager.getDisplay(
                index
            ).state
        }, name: ${displayManager.getDisplay(index).name}"
    )
    //Assertion value
    //when sent CEC ON - displayManager.getDisplay(index).state should be STATE_ON = 2
    hdmiServiceHelper.logger.info("KONGINTEGRATION  - polling now")
    pollForResult(20, 2, TimeUnit.SECONDS) {
        checkDisplayAndAssert(hdmiServiceHelper, index, 2, displayManager)
    }
    hdmiServiceHelper.logger.info("KONGINTEGRATION  - polling completed")
    //Thread.sleep(1000)
    hdmiServiceHelper.logger.info(
        "KONGINTEGRATION post required state 2, but found: - ${
            displayManager.getDisplay(
                index
            ).state
        }, name: ${displayManager.getDisplay(index).name}"
    )

    displayManager.displays.forEach {
        //it.displayId and index might be same, so we can assert if that is the same which got STATE_ON as it.state
        hdmiServiceHelper.logger.info("KONGINTEGRATION - post displays: name - ${it.name}, id - ${it.displayId},state -  ${it.state}, isHDR - ${it.isHdr}")
    }
}
