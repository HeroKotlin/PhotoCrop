package com.github.herokotlin.photocrop.view

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
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
        Log.d("photocrop", "1  size  $size")
        Util.updateSize(imageView, size.width.toInt(), size.height.toInt())
    }

    fun updateImageOrigin() {
        val origin = photoView.imageOrigin
        Log.d("photocrop", "2  origin  $origin   $x,$y")
        Util.updateOrigin(imageView, origin.x - x, origin.y - y)
    }

    fun getTranslateAfterZoom(zoomScale: Float): PointF {

        val oldX = imageView.x
        val oldY = imageView.y

        val relativeX = imageView.x / imageView.width
        val relativeY = imageView.y / imageView.height

        val newX = imageView.width * zoomScale * relativeX
        val newY = imageView.height * zoomScale * relativeY

        return PointF(newX - oldX, newY - oldY)

    }

}