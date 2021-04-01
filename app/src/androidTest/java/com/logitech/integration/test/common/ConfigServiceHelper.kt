package com.logitech.integration.test.common

import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WIFI_STATE_DISABLED
import com.logitech.service.config.ConfigServiceManager
import com.logitech.syncdatamodel.model.device.DeviceControlResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ConfigServiceHelper {

    fun enableWifi() = ConfigServiceManager().enableWiFi(true)

    fun checkWifiConnected(context: Context): Boolean {
        val deviceControlResponse = enableWifi()
        if (!deviceControlResponse.equals(DeviceControlResponse.SUCCESS)) {
            return false
        }
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var isWifiConnected = wifiManager.isWifiEnabled
        runBlocking {
            val job = GlobalScope.launch {
                val startTime = System.currentTimeMillis()
                while(!isWifiConnected && System.currentTimeMillis() < (startTime + 20000L)) {
                    delay(1000L)
                    isWifiConnected = wifiManager.isWifiEnabled
                }
            }
            job.join()
        }
        return isWifiConnected
    }

    fun disableWifi() = ConfigServiceManager().disableWiFi(true)

    fun checkWifiDisconnected(context: Context): Boolean {
        val deviceControlResponse = disableWifi()
        if (!deviceControlResponse.equals(DeviceControlResponse.SUCCESS)) {
             return false
        }
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var isWifiDisconnected = wifiManager.wifiState == WIFI_STATE_DISABLED
        runBlocking {
            val job = GlobalScope.launch {
                val startTime = System.currentTimeMillis()
                while(!isWifiDisconnected && System.currentTimeMillis() < (startTime + 20000L)) {
                    delay(1000L)
                    isWifiDisconnected = wifiManager.wifiState == WIFI_STATE_DISABLED
                }
            }
            job.join()
        }
        return isWifiDisconnected
    }
}