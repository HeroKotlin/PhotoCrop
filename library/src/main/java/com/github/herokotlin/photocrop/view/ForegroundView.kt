package com.github.herokotlin.photocrop.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
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
        Util.updateSize(imageView, size.width, size.height)
    }

    fun updateImageOrigin() {
        val origin = photoView.imageOrigin
        Util.updateOrigin(imageView, origin.x, origin.y)
    }

}