package com.github.herokotlin.photocrop.view

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.github.herokotlin.photocrop.R
import com.github.herokotlin.photocrop.util.Util
import com.github.herokotlin.photoview.PhotoView
import kotlinx.android.synthetic.main.photo_crop_foreground.view.*

class ForegroundView: FrameLayout {

    lateinit var photoView: PhotoView

    private var oldX = 0f
    private var oldY = 0f

    private var relativeX = 0f
    private var relativeY = 0f

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
        Util.updateSize(imageView, size.width.toInt(), size.height.toInt())
    }

    fun updateImageOrigin() {
        val origin = photoView.imageOrigin
        Util.updateOrigin(imageView, origin.x - x, origin.y - y)
    }

    fun save() {

        oldX = imageView.x
        oldY = imageView.y

        relativeX = oldX / imageView.width
        relativeY = oldY / imageView.height

    }

    fun restore(): PointF {

        val newX = imageView.width * relativeX
        val newY = imageView.height * relativeY

        return PointF(newX - oldX, newY - oldY)

    }

}