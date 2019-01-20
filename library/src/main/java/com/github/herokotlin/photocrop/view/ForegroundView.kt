package com.github.herokotlin.photocrop.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.github.herokotlin.photocrop.R
import com.github.herokotlin.photocrop.util.Util
import com.github.herokotlin.photoview.PhotoView
import kotlinx.android.synthetic.main.photo_crop_foreground.view.*

class ForegroundView: FrameLayout {

    lateinit var photoView: PhotoView

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.photo_crop_foreground, this)
    }

    fun bindPhotoView(photoView: PhotoView) {
        this.photoView = photoView
        updateImageSize()
        updateImageOrigin()
    }

    fun updateImageSize() {
        val size = photoView.imageSize
        Log.d("photocrop1", "size $size")
        Util.updateSize(imageView, size.width, size.height)
    }

    fun updateImageOrigin() {
        val origin = photoView.imageOrigin
        Util.updateOrigin(imageView, origin.x, origin.y)
        Log.d("photocrop1", "origin $origin")
    }

}