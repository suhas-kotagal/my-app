package com.logitech.integration.test.helpers.config

import com.logitech.integration.test.helpers.common.begin
import com.logitech.service.config.ConfigServiceManager
import org.slf4j.LoggerFactory
import java.io.Closeable

class ConfigServiceHelper : Closeable {
    val logger = LoggerFactory.getLogger(this.javaClass.name)
    val configManager: ConfigServiceManager by lazy {
        val mgr = ConfigServiceManager().begin()
        mgr
    }

    override fun close() {
    }
}

