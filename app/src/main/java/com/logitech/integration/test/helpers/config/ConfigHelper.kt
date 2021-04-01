package com.logitech.integration.test.helpers.mode

import android.os.IBinder
import com.logitech.service.config.ConfigServiceManager
import com.logitech.service.config.IConfigurationService
import com.logitech.service.config.IDeviceListener
import com.logitech.service.models.mode.DeviceMode
import com.logitech.service.modeservice.IModeListener
import com.logitech.service.modeservice.ModeServiceManager
import com.logitech.syncdatamodel.model.device.*
import io.mockk.mockk
import org.slf4j.LoggerFactory

class ConfigListenerHelper(
    var configService: ConfigServiceManager? = null,
    var listenerMock: IConfigurationService = mockk(relaxed = true)
) : IConfigurationService.Stub() {
    val logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun isReady(): Boolean {
        logger.info("inside isReady()")
        return true
    }

    override fun registerDeviceChangedListener(p0: IDeviceListener?) {
        logger.info("inside registerDeviceChangedListener(): $p0")
    }

    override fun unregisterDeviceChangedListener(p0: IDeviceListener?) {
        logger.info("inside unregisterDeviceChangedListener() $p0")
    }

    override fun updateFirmware(p0: Int) {
        logger.info("inside updateFirmware(): $p0")
    }

    override fun getLastKnownDevice(): Device {
        logger.info("inside getLastKnownDevice()")
        return getLastKnownDevice()
    }

    override fun checkNewFirmware() {
        logger.info("inside checkNewFirmware()")
    }

    override fun setForceDeviceMode(p0: com.logitech.syncdatamodel.model.device.DeviceMode?) {
        logger.info("inside setForceDeviceMode(): $p0")
    }

    override fun rebootDevice(p0: Boolean): DeviceControlResponse {
        logger.info("inside rebootDevice(): $p0")
        return DeviceControlResponse.SUCCESS
    }

    override fun shutdownDevice(p0: Boolean): DeviceControlResponse {
        logger.info("inside shutdownDevice()")
        return DeviceControlResponse.SUCCESS
    }

    override fun enableWiFi(p0: Boolean): DeviceControlResponse {
        logger.info("inside enableWiFi() : $p0")
        return DeviceControlResponse.SUCCESS
    }

    override fun disableWiFi(p0: Boolean): DeviceControlResponse {
        logger.info("inside disableWiFi(): $p0")
        return DeviceControlResponse.SUCCESS
    }

    override fun bindMode(p0: DeviceRemoteMode?, p1: IBinder?): DeviceRemoteMode {
        logger.info("inside bindMode(): $p0")
        return p0!!
    }

    override fun unbindMode(p0: IBinder?) {
        logger.info("inside unbindMode(): $p0")
    }

    override fun setPeriodicCheckPeriod(p0: Int): Boolean {
        logger.info("inside setPeriodicCheckPeriod(): $p0")
        return p0.toBoolean()
    }

    override fun updateTo(p0: String?, p1: Boolean, p2: Boolean): Int {
        logger.info("inside updateTo(): $p0, $p1, $p2")
        return 0
    }

    override fun audioVolumeUp(p0: Boolean): Boolean {
        logger.info("inside audioVolumeUp(): $p0")
        return p0
    }

    override fun audioVolumeDown(p0: Boolean): Boolean {
        logger.info("inside audioVolumeDown(): $p0")
        return p0
    }

    override fun audioMute(p0: Boolean): Boolean {
        logger.info("inside audioMute(): $p0")
        return p0
    }

    override fun audioCall(p0: Boolean): Boolean {
        logger.info("inside audioCall(): $p0")
        return p0
    }

    override fun setAICVMode(p0: DeviceAICVMode?) {
        logger.info("inside setAICVMode(): $p0")
    }

    override fun enableBluetooth(): Boolean {
        logger.info("inside enableBluetooth()")
        return true
    }

    override fun disableBluetooth(): Boolean {
        logger.info("inside disableBluetooth()")
        return true
    }

    override fun scheduleUpdate(p0: UpdateSchedule?): Int {
        logger.info("KONGINTEGRATION inside scheduleUpdate(): $p0")
        return 1
    }

    override fun clearUpdateSchedule(): Boolean {
        TODO("Not yet implemented")
    }
}
fun Int.toBoolean() = this == 1
