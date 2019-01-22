package com.github.herokotlin.photocrop

import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.github.herokotlin.photocrop.model.CropArea
import com.github.herokotlin.photocrop.util.Util
import com.github.herokotlin.photoview.PhotoView
import kotlinx.android.synthetic.main.photo_crop.view.*
import kotlinx.android.synthetic.main.photo_crop_foreground.view.*

class PhotoCrop: FrameLayout {

    private var cropArea = CropArea.zero

        set(value) {
            field = value
            finderView.cropArea = value
        }

    var isCropping = false

        set(value) {

            if (field == value) {
                return
            }

            field = value

            var fromCropArea = cropArea
            var toCropArea = fromCropArea

            var toContentInset = PhotoView.ContentInset.zero

            val fromScale = photoView.scale
            var toScale = fromScale

            var offsetCropArea = CropArea.zero

            val reader: () -> Unit
            val animation: (Float) -> Unit

            if (value) {

                foregroundView.alpha = 1f

                photoView.scaleType = PhotoView.ScaleType.FILL

                fromCropArea = getCropAreaByPhotoView()
                toCropArea = finderView.normalizedCropArea

                toContentInset = toCropArea.toContentInset()

                reader = {

                    toScale = photoView.scale

                }

                animation = { alpha ->

                    overlayView.alpha = alpha
                    finderView.alpha = alpha
                    gridView.alpha = alpha

                    cropArea = fromCropArea.add(offsetCropArea.multiply(alpha))

                }

            }
            else {

                foregroundView.alpha = 0f

                photoView.scaleType = PhotoView.ScaleType.FIT

                reader = {

                    toScale = photoView.scale

                    toCropArea = getCropAreaByPhotoView()

                }

                animation = { value ->

                    val alpha = 1 - value

                    overlayView.alpha = alpha
                    finderView.alpha = alpha
                    gridView.alpha = alpha

                    cropArea = fromCropArea.add(offsetCropArea.multiply(value))

                }

            }

            photoView.contentInset = toContentInset

            photoView.updateForRead({ baseMatrix, changeMatrix ->
                photoView.resetMatrix(baseMatrix, changeMatrix)
            }, reader)

            cropArea = fromCropArea

            offsetCropArea = toCropArea.minus(fromCropArea)

            startAnimation(animation) {
                photoView.updateLimitScale()
            }

            photoView.setFocusPoint(width / 2f, height / 2f)
            photoView.startZoomAnimation(fromScale, toScale)

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
            Util.updateView(foregroundView, rect.left, rect.top, rect.width().toInt(), rect.height().toInt())
            Util.updateView(gridView, rect.left, rect.top, rect.width().toInt(), rect.height().toInt())
            foregroundView.updateImageOrigin()
        }
        finderView.onCropAreaResize = {
            updateCropArea(finderView.normalizedCropArea)
        }

        foregroundView.bindPhotoView(photoView)

        photoView.scaleType = PhotoView.ScaleType.FIT
        photoView.onScaleChange = {
            updateFinderMinSize()
            foregroundView.updateImageSize()
        }
        photoView.onOriginChange = {
            foregroundView.updateImageOrigin()
        }

        photoView.setImageResource(R.drawable.image)
        foregroundView.imageView.setImageResource(R.drawable.image)

    }

    private fun startAnimation(update: (Float) -> Unit, complete: (() -> Unit)? = null) {

        this.activeAnimator?.cancel()

        val animator = ValueAnimator.ofFloat(0f, 1f)

        animator.duration = photoView.zoomDuration
        animator.interpolator = photoView.zoomInterpolator
        animator.addUpdateListener {
            update(it.animatedValue as Float)
        }
        animator.addListener(object: AnimatorListenerAdapter() {
            // 动画被取消，onAnimationEnd() 也会被调用
            override fun onAnimationEnd(animation: android.animation.Animator?) {
                complete?.invoke()
                if (animation == activeAnimator) {
                    activeAnimator = null
                }
            }
        })
        animator.start()

        this.activeAnimator = animator

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

        val left = Math.max(imageOrigin.x, 0f)
        val top = Math.max(imageOrigin.y, 0f)

        val right = Math.max(photoView.width - (imageOrigin.x + imageSize.width), 0f)
        val bottom = Math.max(photoView.height - (imageOrigin.y + imageSize.height), 0f)

        return CropArea(top, left, bottom, right)

    }

    private fun updateCropArea(cropArea: CropArea) {

        val fromCropArea = finderView.cropArea
        val toCropArea = cropArea

        val fromRect = fromCropArea.toRect(width, height)
        val toRect = toCropArea.toRect(width, height)

        // 谁更大就用谁作为缩放系数
        val widthScale = toRect.width() / fromRect.width()
        val heightScale = toRect.height() / fromRect.height()
        val scale = Math.max(widthScale, heightScale)

        if (scale == 1f) {
            return
        }

        val offsetCropArea = toCropArea.minus(fromCropArea)

        val fromScale = photoView.scale
        val toScale = fromScale * scale


        // 获取偏移量
        foregroundView.save()

        this.cropArea = toCropArea

        photoView.setFocusPoint(fromRect.centerX(), fromRect.centerY())
        photoView.zoom(toScale / fromScale, true)

        val translate = foregroundView.restore()


        // 开始动画
        this.cropArea = fromCropArea

        startAnimation({ value ->
            this.cropArea = fromCropArea.add(offsetCropArea.multiply(value))
        })

        photoView.startZoomAnimation(fromScale, toScale)
        photoView.startTranslateAnimation(translate.x, translate.y, photoView.zoomInterpolator)

    }

}