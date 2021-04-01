package com.logitech.integration.test.camera.model

import android.app.Application
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.logitech.integration.test.camera.FocusDataProvider
import org.slf4j.LoggerFactory

class FocusViewModel(application: Application) : AndroidViewModel(application), Observable{
    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }
    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    val focusValue = MutableLiveData<Int>(0)
    val focusMax = MutableLiveData<Int>(399)
    val focusMin = MutableLiveData<Int>(0)
    val autoFocus = MutableLiveData<Boolean>(true)
    val camera2InputFocusValue = MutableLiveData<Int>(0)
    val focusDataProvider = FocusDataProvider

    init {
        focusDataProvider.autoFocus = autoFocus
        focusDataProvider.focusValue = focusValue
        focusDataProvider.focusMax = focusMax
        focusDataProvider.focusMin = focusMin
        focusDataProvider.init()
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.remove(callback)
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.add(callback)
    }

    /**
     * Notifies listeners that all properties of this instance have changed.
     */
    fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    /**
     * Notifies listeners that a specific property has changed. The getter for the property
     * that changes should be marked with [Bindable] to generate a field in
     * `BR` to be used as `fieldId`.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }

}