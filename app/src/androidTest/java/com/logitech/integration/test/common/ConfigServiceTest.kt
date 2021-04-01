package com.logitech.integration.test.common

import com.logitech.service.config.ConfigServiceManager


class ConfigServiceTest {

    fun enableWifi() = ConfigServiceManager().enableWiFi(true)

    fun disableWifi() = ConfigServiceManager().disableWiFi(true)
}