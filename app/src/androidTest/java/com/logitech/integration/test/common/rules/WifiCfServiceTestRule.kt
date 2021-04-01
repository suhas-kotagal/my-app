package com.logitech.integration.test.common.rules


import android.os.RemoteException
import com.logitech.service.config.ConfigServiceManager
import com.logitech.syncdatamodel.model.device.DeviceControlResponse

class WifiCfServiceTestRule {

    private var serviceManager: ConfigServiceManager? = null
    private var response : DeviceControlResponse? = null
    private var volume = false

    fun enableWifi(): DeviceControlResponse? {
        try {
            serviceManager = ConfigServiceManager()
            response = serviceManager!!.enableWiFi(true)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return response
    }

    fun disableWifi(): DeviceControlResponse? {
        serviceManager = ConfigServiceManager()
        try {
            response = serviceManager!!.disableWiFi(false)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return response
    }

    fun audioVolumeUp(): Boolean {
        serviceManager = ConfigServiceManager()
        try {
            volume = serviceManager!!.audioVolumeUp(true)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return volume
    }

    fun audioVolumeDown(): Boolean {
        serviceManager = ConfigServiceManager()
        try {
            volume = serviceManager!!.audioVolumeDown(true)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return volume
    }

    fun audioMute(): Boolean {
        serviceManager = ConfigServiceManager()
        try {
            volume = serviceManager!!.audioMute(true)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return volume
    }

    fun audioCall(): Boolean {
        serviceManager = ConfigServiceManager()
        try {
            volume = serviceManager!!.audioCall(true)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return volume
    }
}