package com.logitech.integration.test.camera.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CameraResults(val fps:Double) : Parcelable {
    companion object {
        const val RESULT_CODE = 0
        const val EXTRA = "camera.result"
    }
}