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

internal class ForegroundView: FrameLayout {

    lateinit var photoView: PhotoView

    var relativeX = 0f
    var relativeY = 0f
    var relativeWidth = 0f
    var relativeHeight = 0f

    // 貌似同步执行时，Util.updateSize() 之后立即获取不到正确的尺寸
    // 因此这里存个变量，方便获取
    private var imageSize = PhotoView.Size(0f, 0f)

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

    fun updateImageSize() {
        imageSize = photoView.imageSize
        Util.updateSize(imageView, imageSize.width.toInt(), imageSize.height.toInt())
    }

    fun updateImageOrigin() {
        val origin = photoView.imageOrigin
        Util.updateOrigin(imageView, origin.x - x, origin.y - y)
    }

    fun save() {

        relativeX = imageView.x / imageSize.width
        relativeY = imageView.y / imageSize.height

        relativeWidth = width / imageSize.width
        relativeHeight = height / imageSize.height

    }

    fun restore(): PointF {

        val newX = imageSize.width * relativeX
        val newY = imageSize.height * relativeY

        return PointF(newX - imageView.x, newY - imageView.y)

    }

}