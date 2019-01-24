package com.github.herokotlin.photocrop

import android.app.Activity
import com.github.herokotlin.photocrop.model.CropFile

interface PhotoCropCallback {

    fun onCancel(activity: Activity)

    fun onSubmit(activity: Activity, cropFile: CropFile)

}