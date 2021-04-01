/*package com.logitech.integration.test.hdmi

import com.logitech.integration.test.helpers.hdmi.HDMIServiceHelper
import com.logitech.integration.test.helpers.hdmi.sendCEC
import org.junit.Test


/**
 *  adb shell am instrument -w -r -e class 'com.logitech.integration.test.hdmi.HdmiTest' com.logitech.integration.test/androidx.test.runner.AndroidJUnitRunner
 *  #Run with coverage
 *
 *  ./gradlew createDebugCoverageReport
 *  adb shell am instrument -w -r -e coverageFile /data/data/com.logitech.integration.test/coverage.ec -e coverage true -e class 'com.logitech.integration.test.hdmi.HdmiTest' com.logitech.integration.test.test/androidx.test.runner.AndroidJUnitRunner
 *  adb shell am instrument -w -r -e coverageFile /data/data/com.logitech.integration.test/coverage.ec -e coverage true -e class 'com.logitech.integration.test.hdmi.HdmiTest' com.logitech.integration.test.test/androidx.test.runner.AndroidJUnitRunner
 * START FLAGS IS: cn = ComponentInfo{com.logitech.integration.test.test/androidx.test.runner.AndroidJUnitRunner}, pf = null, flags=0, args=Bundle[{coverageFile=/data/data/com.logitech.integration.test/coverage.ec, coverage=true, class=com.logitech.integration.test.hdmi.HdmiTest}], watcher=com.android.commands.am.Instrument$InstrumentationWatcher@9cfdba6, connection=android.app.UiAutomationConnection@ad149e7, userId = -2, abi=null
 */

class HdmiTest() {

    @Test
    fun hdmiCecTest() {
        HDMIServiceHelper().use { hdmiServiceHelper ->
            sendCEC(hdmiServiceHelper)
        }
    }
}
*/
