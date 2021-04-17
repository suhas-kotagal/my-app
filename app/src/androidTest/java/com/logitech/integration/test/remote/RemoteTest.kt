package com.logitech.integration.test.remote

import androidx.test.filters.MediumTest
import com.logitech.integration.test.common.rules.RemoteRule
import com.logitech.integration.test.helpers.remote.modeChangeAndAssert
import org.junit.Rule
import org.junit.Test

/**
 *  adb shell am instrument -w -r -e class 'com.logitech.integration.test.remote.RemoteTest' com.logitech.integration.test/androidx.test.runner.AndroidJUnitRunner
 *  #Run with coverage
 *
 *  ./gradlew createDebugCoverageReport
 *  adb shell am instrument -w -r -e coverageFile /data/data/com.logitech.integration.test/coverage.ec -e coverage true -e class 'com.logitech.integration.test.remote.RemoteTest' com.logitech.integration.test.test/androidx.test.runner.AndroidJUnitRunner
 *  adb shell am instrument -w -r -e coverageFile /data/data/com.logitech.integration.test/coverage.ec -e coverage true -e class 'com.logitech.integration.test.remote.RemoteTest' com.logitech.integration.test.test/androidx.test.runner.AndroidJUnitRunner
 * START FLAGS IS: cn = ComponentInfo{com.logitech.integration.test.test/androidx.test.runner.AndroidJUnitRunner}, pf = null, flags=0, args=Bundle[{coverageFile=/data/data/com.logitech.integration.test/coverage.ec, coverage=true, class=com.logitech.integration.test.remote.RemoteTest}], watcher=com.android.commands.am.Instrument$InstrumentationWatcher@9cfdba6, connection=android.app.UiAutomationConnection@ad149e7, userId = -2, abi=null
 */

class RemoteTest() {

    @get:Rule
    val remoteRule = RemoteRule()

    @Test
    @MediumTest
    fun remoteTest() {
        remoteRule.executingRemoteTests { remoteServiceHelper ->
            modeChangeAndAssert(remoteServiceHelper)
        }
    }
}
