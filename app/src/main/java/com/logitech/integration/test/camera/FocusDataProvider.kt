package com.logitech.integration.test.camera

import androidx.lifecycle.MutableLiveData
import com.logitech.integration.test.helpers.ptzf.PtzServiceHelper
import com.logitech.integration.test.helpers.ptzf.PtzfRangeWrapper
import com.logitech.integration.test.helpers.ptzf.PtzfTypeWrapper
import com.logitech.integration.test.helpers.ptzf.SimplePtzfListener
import org.slf4j.LoggerFactory
import java.util.EnumSet

object FocusDataProvider : SimplePtzfListener(EnumSet.of(PtzfTypeWrapper.FOCUS), PtzServiceHelper()){
    var focusValue : MutableLiveData<Int>? = null
    var focusMax : MutableLiveData<Int>? = null
    var focusMin : MutableLiveData<Int>? = null
    var autoFocus : MutableLiveData<Boolean>? = null
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    init {
        ptzServiceHelper.registerListener(this)
    }

    fun init(){
        val range = ptzServiceHelper.getFocusRange()
        focusMax?.postValue(range.max)
        focusMin?.postValue(range.min)
        focusValue?.postValue(getFocus())
        logger.info("focus values: min,max=[{},{}] current={}", focusMin?.value, focusMax?.value, focusValue?.value)
        logger.info("ptzf = {}, {}, {}, {} ", ptzServiceHelper.getAbsolute(PtzfTypeWrapper.PAN), ptzServiceHelper.getAbsolute(PtzfTypeWrapper.TILT), ptzServiceHelper.getAbsolute(PtzfTypeWrapper.ZOOM), ptzServiceHelper.getAbsolute(PtzfTypeWrapper.FOCUS))
    }

    fun getRange(): PtzfRangeWrapper {
        return ptzServiceHelper.getFocusRange()
    }

    fun getFocus() = ptzServiceHelper.getAbsolute(PtzfTypeWrapper.FOCUS)

    fun setFocus(value:Int){
        ptzServiceHelper.setAbsolute(PtzfTypeWrapper.FOCUS, value)
    }
    override fun onValueChange(type: PtzfTypeWrapper, value: Int) {
        logger.info("focus changed: newValue={}", value)
        when(type){
            PtzfTypeWrapper.FOCUS -> focusValue?.postValue(value)
            else -> logger.debug("{} not currently mapped to live data type", type)
        }
        logger.info("focus changed: updated={}", focusValue?.value)
    }
}