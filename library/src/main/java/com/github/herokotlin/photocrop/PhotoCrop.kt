package com.github.herokotlin.photocrop

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.github.herokotlin.photocrop.util.Util
import com.github.herokotlin.photoview.PhotoView
import com.github.herokotlin.photoview.PhotoViewCallback
import kotlinx.android.synthetic.main.photo_crop.view.*

class PhotoCrop: FrameLayout {

    var isCropping = false

        set(value) {

            if (field == value) {
                return
            }

            field = value

            if (value) {

                Util.showView(overlayView)
                Util.showView(finderView)
                Util.showView(gridView)

                overlayView.alpha = 0f
                finderView.alpha = 0f
                gridView.alpha = 0f

                photoView.scaleType = PhotoView.ScaleType.FILL

                // 初始化裁剪区域，尺寸和当前图片一样大
                // 这样就会有一个从大到小的动画

            }
        }

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
        LayoutInflater.from(context).inflate(R.layout.photo_crop, this)

        finderView.onCropAreaResize = {
            finderView.cropArea = finderView.normalizedCropArea
        }
        finderView.onCropAreaChange = {
            val rect = finderView.cropArea.toRect(width, height)
            Util.updateView(gridView, rect.left, rect.top, rect.width(), rect.height())
        }

        photoView.callback = object: PhotoViewCallback {

        }

        photoView.scaleType = PhotoView.ScaleType.FIT
        photoView.setImageResource(R.drawable.image)
    }

}