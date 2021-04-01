package com.logitech.integration.test.helpers.ptzf


import com.logitech.integration.test.helpers.common.begin
import com.logitech.integration.test.helpers.common.end
import com.logitech.service.models.ptzf.PtzfInput
import com.logitech.service.models.ptzf.PtzfRange
import com.logitech.service.models.ptzf.PtzfType
import com.logitech.service.ptzfservice.PtzfServiceManager
import org.slf4j.LoggerFactory
import java.io.Closeable

enum class PtzfTypeWrapper(val ptzfType: PtzfType) {
    PAN(PtzfType.Pan), TILT(PtzfType.Tilt), ZOOM(PtzfType.Zoom), FOCUS(PtzfType.Focus);
    companion object{
        fun fromType(type: PtzfType) : PtzfTypeWrapper{
            return values().filter { type == it.ptzfType }.first()
        }
    }
}

data class PtzfRangeWrapper(val min: Int, val max:Int, val resolution:Int)
data class PtzfRangeSetWrapper(val panRange: PtzfRangeWrapper, val tiltRange: PtzfRangeWrapper, val zoomRange: PtzfRangeWrapper)


/**
 * Helps gets data from the service. Service APIs are internal and should not therefore expose the com.logitech.service namespace outside of this class
 *
 * + Some features in blueshell are incomplete, so this class also services as a helper to accomplish some things that are not support in blueshell (such as ptz motion events)
 */
class PtzServiceHelper(val ptzListenerHelper : PtzListenerHelper = PtzListenerHelper(), val listeners: MutableList<PtzListenerWrapper> = mutableListOf()) : Closeable {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    private val ptzManager: PtzfServiceManager by lazy {
        val mgr = PtzfServiceManager().begin<PtzfServiceManager>()
        listeners.forEach{
            mgr.registerListener(it)
            it.isRegistered.set(true)
        }
        ptzListenerHelper.isRegistered.set(true)
        mgr.registerListener(ptzListenerHelper)
        ptzListenerHelper.ptzService = mgr
        mgr
    }

    fun init () : Boolean = ptzManager.isReady

    fun registerListener(listener: PtzListenerWrapper){
        listeners.add(listener)
        ptzManager.registerListener(listener)
        listener.isRegistered.set(true)
    }

    fun unregister(){
        ptzListenerHelper.isRegistered.set(false)
        synchronized(ptzListenerHelper) {
            logger.info("Unregistering listener {}", ptzListenerHelper)
            ptzManager.unregisterListener(ptzListenerHelper)
        }
        listeners.forEach{
            ptzManager.unregisterListener(it)
        }
    }

    fun getRangeSet() = PtzfRangeSetWrapper(
        getPanRange(),
        getTiltRange(),
        getZoomRange()
    )

    fun setAbsolute(ptzType: PtzfTypeWrapper, value:Int) {
        ptzManager.setAbsolute(PtzfInput.BLueshell, ptzType.ptzfType, value)
    }

    fun getAbsolute(ptzType: PtzfTypeWrapper) = ptzManager.getAbsolute(ptzType.ptzfType)


    fun getPanRange() = ptzManager.getRange(PtzfType.Pan).toRange()

    fun getTiltRange() = ptzManager.getRange(PtzfType.Tilt).toRange()

    fun getZoomRange() = ptzManager.getRange(PtzfType.Zoom).toRange()

    fun getFocusRange() = ptzManager.getRange(PtzfType.Focus).toRange()

    override fun close() {
        try {
            ptzManager.end()
        }catch (e: IllegalArgumentException) {}
    }
}

fun PtzfRange.toRange() : PtzfRangeWrapper {
    return PtzfRangeWrapper(
        this.min,
        this.max,
        this.resolution
    )
}