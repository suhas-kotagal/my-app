package com.logitech.integration.test.helpers.ptzf

import com.logitech.service.models.ptzf.PtzfType
import logitech.hardware.camera.PtzDevice
import org.junit.Assert
import java.lang.Math.abs
import java.util.EnumSet
import java.util.concurrent.TimeUnit

/**
 * Additional helpers for PtzDevice for junit assertions
 *
 */
fun Float.equalsDelta(other: Float, delta:Float = FLOAT_DELTA) = abs(this - other) < delta

fun PtzDevice.assertPosition(value:Float, ptzType: PtzfTypeWrapper) {
    when(ptzType){
        PtzfTypeWrapper.ZOOM -> assertPositions(zoom = value)
        PtzfTypeWrapper.PAN -> assertPositions(pan = value)
        PtzfTypeWrapper.TILT -> assertPositions(tilt = value)
    }
}

fun PtzDevice.assertFloatPosition(value: Float? = null, positionValue: Float) {
    //value?.let { Assert.assertEquals(positionValue, value, FLOAT_DELTA) }
}

fun PtzDevice.assertPositions(pan:Float? = null, tilt:Float? = null, zoom:Float? = null) {
    //pan?.let { Assert.assertEquals(panPosition, pan, FLOAT_DELTA) }
    //tilt?.let { Assert.assertEquals(tiltPosition, tilt, FLOAT_DELTA) }
    //zoom?.let { Assert.assertEquals(zoomPosition, zoom, FLOAT_DELTA) }
}

//fun applyAndAssert(ptzType: PtzType, value:Int, timeout:Long = 100, timeUnit:TimeUnit = TimeUnit.SECONDS) {
//    ptzListener.waitForPositionChange(EnumSet.of(ptzType.ptzfType), timeout, timeUnit, {setAbsolute(ptzType, value)})
//    assertEquals(value, ptzManager.getAbsolute(ptzType.ptzfType))
//}


fun PtzDevice.centerAndAssert(ptzServiceHelper: PtzServiceHelper, timeout : Long = 100, timeUnit: TimeUnit = TimeUnit.SECONDS){
    logger().info("Centering the device")
    applyMovementAndAssert(ptzServiceHelper, PtzDevice.MOVE_ABSOLUTE, 0F, 0F, 290f)
    logger().info("Device is centered.")
}

/**
 * Move the camera to a PTZ position and verify that the camera is reporting that it made it to the expected position
 *
 */
fun PtzDevice.applyMovementAndAssert(ptzServiceHelper: PtzServiceHelper, movementType: Int, panValue: Float, tiltValue: Float, zoomValue: Float, timeout : Long = 30, timeUnit: TimeUnit = TimeUnit.SECONDS){
    logger().info("Setting movement values P/T/Z = {}/{}/{}", panValue, tiltValue, zoomValue)
    val valuesNotInPosition = getNonPositionedSet(panValue, tiltValue, zoomValue)
    setPosition(movementType, panValue, tiltValue, zoomValue)
    // if no positions need to be changed we move on
    if (!valuesNotInPosition.isEmpty()) {
        logger().info("Values that are not in correct state : {}", valuesNotInPosition)
        when(ptzServiceHelper.ptzListenerHelper) {
            is PtzListenerHelper -> (ptzServiceHelper.ptzListenerHelper as PtzListenerHelper).waitForPositionChange(
                valuesNotInPosition, timeout, timeUnit
            )
        }
        //assertPositions(panValue, tiltValue, zoomValue)
    }
}

fun PtzDevice.applyPanMovementAndAssert(ptzServiceHelper: PtzServiceHelper, movementType: Int, panValue: Float, speed: Float, timeout: Long = 30, timeUnit: TimeUnit = TimeUnit.SECONDS) {
    logger().info("Setting pan value $panValue with movementType: $movementType")
    val valuesNotInPosition = getNonPositionedSet(panValue, PtzfType.Pan, panPosition)
    pan(movementType, panValue, speed)
    // if no positions need to be changed we move on
    if (!valuesNotInPosition.isEmpty()) {
        logger().info("Values that are not in correct state : {}", valuesNotInPosition)
        ptzServiceHelper.ptzListenerHelper.waitForPositionChange(
            valuesNotInPosition, timeout, timeUnit
        )
        assertFloatPosition(panValue, panPosition)
    }
}

fun PtzDevice.applyTiltMovementAndAssert(ptzServiceHelper: PtzServiceHelper, movementType: Int, tiltValue: Float, speed: Float, timeout: Long = 30, timeUnit: TimeUnit = TimeUnit.SECONDS) {
    logger().info("Setting tilt value $tiltValue with movementType: $movementType")
    val valuesNotInPosition = getNonPositionedSet(tiltValue, PtzfType.Tilt, tiltPosition)
    tilt(movementType, tiltValue, speed)
    // if no positions need to be changed we move on
    if (!valuesNotInPosition.isEmpty()) {
        logger().info("Values that are not in correct state : {}", valuesNotInPosition)
        ptzServiceHelper.ptzListenerHelper.waitForPositionChange(
            valuesNotInPosition, timeout, timeUnit
        )
        assertFloatPosition(tiltValue, tiltPosition)
    }
}

fun PtzDevice.applyZoomMovementAndAssert(ptzServiceHelper: PtzServiceHelper, movementType: Int, zoomValue: Float, speed: Float, timeout: Long = 30, timeUnit: TimeUnit = TimeUnit.SECONDS) {
    logger().info("Setting zoom value $zoomValue with movementType: $movementType")
    val valuesNotInPosition = getNonPositionedSet(zoomValue, PtzfType.Zoom, zoomPosition)
    zoom(movementType, zoomValue)
    // if no positions need to be changed we move on
    if (!valuesNotInPosition.isEmpty()) {
        logger().info("Values that are not in correct state : {}", valuesNotInPosition)
        ptzServiceHelper.ptzListenerHelper.waitForPositionChange(
            valuesNotInPosition, timeout, timeUnit
        )
        if (zoomValue == -1f) assertFloatPosition(100f, zoomPosition)
        else assertFloatPosition(zoomValue, zoomPosition)
    }
}

/**
 * These APIs are broken today.
 */
fun PtzDevice.applyMovementAndAssert(ptzServiceHelper: PtzServiceHelper, value: Float, movementType: PtzfTypeWrapper, timeout : Long = 100, timeUnit: TimeUnit = TimeUnit.SECONDS){
    TODO("Don't use, these APIs do not work. This is a BlusShell issue")
    when(ptzServiceHelper.ptzListenerHelper){
        is PtzListenerHelper -> (ptzServiceHelper.ptzListenerHelper as PtzListenerHelper).waitForPositionChange(EnumSet.of(PtzfType.Pan), timeout, timeUnit, {
            when(movementType){
                PtzfTypeWrapper.PAN -> pan(
                    MOVE_TYPE, value,
                    SPEED_IS_REQUIRED_BUT_NOT_USED
                )
                PtzfTypeWrapper.TILT -> tilt(
                    MOVE_TYPE, value,
                    SPEED_IS_REQUIRED_BUT_NOT_USED
                )
                PtzfTypeWrapper.ZOOM -> zoom(
                    MOVE_TYPE, value)
            }
        })
    }

    assertPosition(value, movementType)
}

