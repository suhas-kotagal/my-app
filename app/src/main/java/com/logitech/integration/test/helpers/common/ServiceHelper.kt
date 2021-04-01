package com.logitech.integration.test.helpers.common

import android.os.IInterface
import com.logitech.service.baseservice.BaseServiceManager

fun <T : BaseServiceManager<*>> T.begin() : T {
    bind()
    init()
    start()
    return this
}

fun BaseServiceManager<out IInterface>.end(){
    stop(false)
    unbind()
}

