package com.logitech.integration.test.helpers.remote

import android.os.Binder
import android.os.IBinder
import com.logitech.service.models.remote.RemoteMode
import com.logitech.integration.test.common.VERIFY_TIMEOUT_MS
import io.mockk.verify
import org.junit.Assert

fun modeChangeAndAssert(remoteServiceHelper: RemoteServiceHelper) {
    val remoteBinder: IBinder = Binder()
    val currentMode = remoteServiceHelper.remoteManager.remoteMode
    //TODO - If Remote mode is not in PTZ, we cannot unbind/bind using our local remoteBinder,
    // Results in "W Logi_RemoteS: Binder does not match" and the test run will be interrupted.
    if (currentMode != RemoteMode.ptz) {
        remoteServiceHelper.remoteListener.logger.info("Cannot run Remote test cases in: $currentMode mode")
        return
    }
    changeRemoteModeAndAssert(remoteServiceHelper, RemoteMode.ui, remoteBinder)
    changeRemoteModeAndAssert(remoteServiceHelper, RemoteMode.teams, remoteBinder)
}


fun changeRemoteModeAndAssert(
    remoteServiceHelper: RemoteServiceHelper,
    remoteMode: RemoteMode,
    remoteBinder: IBinder
) {
    remoteServiceHelper.remoteManager.bindMode(remoteMode, remoteBinder)
    verify(timeout = VERIFY_TIMEOUT_MS) {
        remoteServiceHelper.remoteListener.listenerMock.onModeChanged(remoteMode)
    }
    Assert.assertEquals(remoteMode, remoteServiceHelper.remoteManager.remoteMode)

    remoteServiceHelper.remoteManager.unbindMode(remoteBinder)
    verify(timeout = VERIFY_TIMEOUT_MS) {
        remoteServiceHelper.remoteListener.listenerMock.onModeChanged(RemoteMode.ptz)
    }
    Assert.assertEquals(RemoteMode.ptz, remoteServiceHelper.remoteManager.remoteMode)
}
