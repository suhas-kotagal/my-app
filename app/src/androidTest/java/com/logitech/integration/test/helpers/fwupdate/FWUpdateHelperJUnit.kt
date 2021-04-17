package com.logitech.integration.test.helpers.fwupdate

fun getFWPeriodicCheck(): Int {
    FWUpdateServiceHelper().use { fwUpdateServiceHelper ->
        return fwUpdateServiceHelper.fwupdateManager.periodicCheckPeriod
    }
}
