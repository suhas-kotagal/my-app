package com.logitech.integration.test.helpers.remote

import com.logitech.integration.test.helpers.common.begin
import com.logitech.integration.test.helpers.remote.RemoteListenerHelper
import com.logitech.service.remoteservice.RemoteServiceManager
import java.io.Closeable


class RemoteServiceHelper : Closeable {
    val remoteManager: RemoteServiceManager by lazy {
        val mgr = RemoteServiceManager().begin()
        remoteListener.isRegistered.set(true)
        mgr.registerListener(remoteListener)
        remoteListener.remoteManager = mgr
        mgr
    }
    val remoteListener = RemoteListenerHelper()

    override fun close() {
    }
}