package com.logitech.integration.test.helpers.ptzf

import com.logitech.service.models.ptzf.PtzfData
import com.logitech.service.models.ptzf.PtzfInput
import com.logitech.service.models.ptzf.PtzfType
import com.logitech.service.ptzfservice.IPtzfServiceListener
import com.logitech.service.ptzfservice.PtzfServiceManager
import logitech.hardware.camera.PtzDevice
import logitech.hardware.camera.PtzDeviceEvent
import org.slf4j.LoggerFactory
import java.lang.Math.abs
import java.util.Arrays
import java.util.EnumSet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


const val SPEED_IS_REQUIRED_BUT_NOT_USED = 1.0f
const val MOVE_TYPE = PtzDevice.MOVE_ABSOLUTE
const val FLOAT_DELTA = 0.0001F

val functionMap = mapOf(
    PtzfData::getFocus to PtzfType.Focus,
    PtzfData::getZoom to PtzfType.Zoom,
    PtzfData::getTilt to PtzfType.Tilt,
    PtzfData::getPan to PtzfType.Pan
)

fun Float.equalsDelta(other: Float, delta:Float = FLOAT_DELTA) = abs(this - other) < delta

typealias UpdateHandler = (PtzfData) -> Unit

interface PtzfChangedHandler {
    fun onValueChange(type : PtzfTypeWrapper, value:Int)
}

sealed class PtzListenerWrapper() : IPtzfServiceListener.Stub(){
    val isRegistered = AtomicBoolean(true)
    override fun onPtzPositionUpdate(ptzfInput: PtzfInput) {}
}

abstract class SimplePtzfListener(val requestedType : EnumSet<PtzfTypeWrapper>, val ptzServiceHelper: PtzServiceHelper) : PtzListenerWrapper(), PtzfChangedHandler{
    val lastValues = PtzfTypeWrapper.values().map { it to -1 }.toMap().toMutableMap()

    override fun onUpdate(ptzfData: PtzfData) {

        functionMap
            .filter { requestedType.contains(PtzfTypeWrapper.fromType(it.value)) }
            .forEach{
                val ptzfType = PtzfTypeWrapper.fromType(it.value)
                val lastValue = lastValues[ptzfType]
                val newValue = ptzServiceHelper.getAbsolute(ptzfType)
                lastValues[ptzfType] = newValue
                if (lastValue != newValue){
                    onValueChange(ptzfType, newValue)
                }
            }
    }
}

class PtzListenerHelper(var ptzService : PtzfServiceManager? = null) : PtzListenerWrapper(){
    val logger = LoggerFactory.getLogger(this.javaClass.name)
    var ptzfData = PtzfData.Builder().setFocus(0).setPan(0).setZoom(0).setTilt(0).build()
    val ptzfExpected = EnumSet.noneOf(PtzfType::class.java)
    val dirty = EnumSet.noneOf(PtzfType::class.java)
    var latch = CountDownLatch(1)
    var lastState = arrayOf(0, 0, 0, 0)
    var latchReleaseMethod : UpdateHandler? = null

    @Synchronized
    override fun onUpdate(ptzStateChange: PtzfData) {
        if (!isRegistered.get()){
            return
        }
        // Keep track of which updates we have received.
        functionMap
            .filter { ptzfExpected.contains(it.value) && it.key.invoke(ptzStateChange) == 1 }
            .forEach { dirty.add(it.value)}

        // Keep track of the current state.
        val currentArray = toArray(ptzStateChange)
        if (!Arrays.equals(currentArray, lastState)){
            logger.info("PTZ Moving = ${stateChangeToReadable(ptzStateChange)}")
            listOf(PtzfType.Focus, PtzfType.Pan, PtzfType.Tilt, PtzfType.Zoom).forEach {
                logger.info("\tPtz $it --> ${ptzService?.getAbsolute(it)}")
            }
            lastState = currentArray
        }
        ptzfData = ptzStateChange
        logger.info("KONGINTEGRATION - ptz is = {}", asString())

        latchReleaseMethod?.invoke(ptzStateChange)
    }
    private fun stateChangeToReadable(ptzStateChange: PtzfData) : String {
        val result = mutableListOf<String>()
        PtzfType.values().forEach {ptzfType ->
            val function = functionMap.filter { it.value == ptzfType}.keys.first()
            if (function.invoke(ptzStateChange) == 1){
                result.add(function.name.replace("get", "").toUpperCase())
            }

        }
        return if (result.isEmpty()) "none" else result.joinToString(", ")
    }
    private fun toArray(ptzStateChange: PtzfData) : Array<Int>{
        return arrayOf(ptzStateChange.focus, ptzStateChange.pan, ptzStateChange.tilt, ptzStateChange.zoom)
    }

    private fun isNothingMoving() = ptzfData.focus == 0 && ptzfData.zoom == 0 && ptzfData.pan == 0 && ptzfData.tilt == 0;


    fun waitForValue(ptzType: PtzfType, expectedValue:Int, closure: (()->Unit)? = null){
        latch = CountDownLatch(1)
        dirty.clear()
        ptzfExpected.clear()
        ptzfExpected.add(ptzType)
        latchReleaseMethod = { ptzStateChange ->
            //if (ptzStateChange.focus)
            val value = functionMap.filterValues { it == ptzType }.keys.first().invoke(ptzStateChange)
            if (value == expectedValue){
                countDownFailOnTimeout(latch, closure)
            }
        }
    }
    /**
     *  Wait for position changes to become in motion and then settle back down to not in motion. A closure maybe supplied to
     *  avoid multithread complications with the callbacks.
     * */
    fun waitForPositionChange(positionSet:EnumSet<PtzfType>, timeout:Long, timeUnit: TimeUnit, closure: (()->Unit)? = null){
        require(positionSet.isNotEmpty()) {"Test failure, position set cannot be empty"}
        latch = CountDownLatch(1)
        dirty.clear()
        ptzfExpected.clear()
        ptzfExpected.addAll(positionSet)
        latchReleaseMethod = { _ ->
            if (dirty.size == ptzfExpected.size && isNothingMoving()){
                countDownFailOnTimeout(latch)
            }
        }
        closure?.invoke()
        latch.await(timeout, timeUnit);
    }

    private fun countDownFailOnTimeout(countDownLatch: CountDownLatch, onTimeout:(()->Unit)? = null){
        try {
            countDownLatch.countDown()
        } catch (e: InterruptedException){
            onTimeout?.invoke()
        }
    }

    fun asString() : String{
        return "P = ${ptzService?.getAbsolute(PtzfType.Pan)}, T = ${ptzService?.getAbsolute(PtzfType.Tilt)}, Z = ${ptzService?.getAbsolute(PtzfType.Zoom)}, F = ${ptzService?.getAbsolute(PtzfType.Focus)}"
    }

}

// TODO: Many of Blueshells object do not support toString()
fun PtzDeviceEvent.asString() : String{
    return "Pan = ${pan}, PanSpeed = ${panSpeed}, Tilt = ${tilt}, TiltSpeed = ${tiltSpeed}, Zoom = ${zoom}, ZoomSpeed = ${zoomSpeed}, timestamp = ${timestamp}"
}

fun PtzDevice.asString() : String {
    return "Pan = ${panPosition}, PanSpeed = ${panSpeed}, Tilt = ${tiltPosition}, TiltSpeed = ${tiltSpeed}, Zoom = ${zoomPosition}"
}

fun PtzDevice.logger()  = LoggerFactory.getLogger(this.javaClass.name)

fun PtzDevice.getNonPositionedSet(value: Float, type: PtzfType, position: Float): EnumSet<PtzfType> {
    val enumSet = EnumSet.noneOf(PtzfType::class.java)
    if (!value.equalsDelta(position)) {
        enumSet.add(type)
    }
    return enumSet
}

/** Returns the set of positions PTZF that are not in the expected position */
fun PtzDevice.getNonPositionedSet(panValue: Float, tiltValue: Float, zoomValue: Float) : EnumSet<PtzfType>{
    val enumSet = EnumSet.noneOf(PtzfType::class.java)
    if (!panValue.equalsDelta(panPosition)){
        enumSet.add(PtzfType.Pan)
    }
    if (!tiltValue.equalsDelta(tiltPosition)){
        enumSet.add(PtzfType.Tilt)
    }
    if (!zoomValue.equalsDelta(zoomPosition)){
        enumSet.add(PtzfType.Zoom)
    }
    logger().info("Current position P/T/Z = {}/{}/{}", panPosition, tiltPosition, zoomPosition)
    return enumSet
}
