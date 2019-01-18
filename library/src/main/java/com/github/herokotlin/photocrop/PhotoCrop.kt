package com.github.herokotlin.photocrop

import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.github.herokotlin.photocrop.model.CropArea
import com.github.herokotlin.photocrop.util.Util
import com.github.herokotlin.photoview.PhotoView
import kotlinx.android.synthetic.main.photo_crop.view.*

class PhotoCrop: FrameLayout {

    private var cropArea = CropArea.zero

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
                cropArea = getCropAreaByPhotoView()
            }
            else {

                photoView.scaleType = PhotoView.ScaleType.FIT

                // 从选定的裁剪区域到图片区域的动画

            }
        }

    private var activeAnimator: ValueAnimator? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private val finderCornerLineWidth: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.photo_crop_finder_corner_line_width)
    }

    private val finderMinWidth: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.photo_crop_finder_min_width)
    }

    private val finderMinHeight: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.photo_crop_finder_min_height)
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.photo_crop, this)

        finderView.onCropAreaChange = {
            val rect = finderView.cropArea.toRect(width, height)
            Util.updateView(gridView, rect.left, rect.top, rect.width(), rect.height())
        }
        finderView.onCropAreaResize = {
            updateCropArea(finderView.normalizedCropArea)
        }

        photoView.scaleType = PhotoView.ScaleType.FIT
        photoView.onScaleChange = {
            updateFinderMinSize()
        }

        photoView.setImageResource(R.drawable.image)
    }

    private fun updateFinderMinSize() {

        finderView.updateMinSize(
            photoView.maxScale / photoView.scale,
            finderMinWidth,
            finderMinHeight
        )

    }

    // CropArea 完全覆盖 PhotoView
    private fun getCropAreaByPhotoView(): CropArea {

        val imageOrigin = photoView.imageOrigin
        val imageSize = photoView.imageSize

        val left = Math.max(imageOrigin.x, 0)
        val top = Math.max(imageOrigin.y, 0)

        val right = Math.max(photoView.width - (imageOrigin.x + imageSize.width), 0)
        val bottom = Math.max(photoView.height - (imageOrigin.y + imageSize.height), 0)

        return CropArea(top, left, bottom, right)

    }

    private fun updateCropArea(cropArea: CropArea) {

        val oldRect = finderView.cropArea.toRect(width, height)
        val newRect = cropArea.toRect(width, height)

        // 谁更大就用谁作为缩放系数
        val widthScale = newRect.width() / oldRect.width()
        val heightScale = newRect.height() / oldRect.height()
        val scale = Math.max(widthScale, heightScale)

        if (scale == 1) {
            return
        }

        val oldValue = photoView.scale
        val newValue = oldValue * scale
        val animator = ValueAnimator.ofFloat(oldValue, newValue)

        photoView.startZoomAnimator(oldValue, newValue, 500, LinearInterpolator())

//                animator.duration = 500
//                animator.addUpdateListener {
//                    val value = it.animatedValue as Float
//
//                    lastValue = value
//                }
//                animator.addListener(object: AnimatorListenerAdapter() {
//                    // 动画被取消，onAnimationEnd() 也会被调用
//                    override fun onAnimationEnd(animation: android.animation.Animator?) {
//                        if (animation == activeAnimator) {
//                            activeAnimator = null
//                        }
//                    }
//                })
//
//                this.activeAnimator = animator

    }

}