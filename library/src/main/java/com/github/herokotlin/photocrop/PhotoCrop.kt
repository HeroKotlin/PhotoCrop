package com.github.herokotlin.photocrop

import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.github.herokotlin.photocrop.model.CropArea
import com.github.herokotlin.photocrop.util.Util
import com.github.herokotlin.photoview.PhotoView
import kotlinx.android.synthetic.main.photo_crop.view.*
import kotlinx.android.synthetic.main.photo_crop_foreground.view.*
import android.graphics.drawable.BitmapDrawable
import android.view.animation.DecelerateInterpolator

class PhotoCrop: FrameLayout {

    var isCropping = false

        set(value) {

            if (field == value) {
                return
            }

            field = value

            finderView.stopInteraction()

            var fromCropArea = finderView.cropArea
            var toCropArea = fromCropArea

            var toContentInset = PhotoView.ContentInset.zero

            val fromScale = photoView.scale
            var toScale = fromScale

            var offsetCropArea = CropArea.zero

            val reader: () -> Unit
            val animation: (Float) -> Unit
            val complete: () -> Unit

            if (value) {

                foregroundView.alpha = 1f

                photoView.scaleType = PhotoView.ScaleType.FILL

                fromCropArea = getCropAreaByPhotoView()
                toCropArea = finderView.normalizedCropArea

                toContentInset = toCropArea.toContentInset()

                reader = {

                    toScale = photoView.scale

                }

                val overlayFromAlpha = overlayView.alpha
                val overlayOffsetAlpha = configuration.overlayAlphaNormal - overlayFromAlpha


                animation = {

                    overlayView.alpha = overlayFromAlpha + it * overlayOffsetAlpha
                    finderView.alpha = it

                    finderView.cropArea = fromCropArea.add(offsetCropArea.multiply(it))

                }

                complete = {
                    photoView.updateLimitScale()
                }

            }
            else {

                gridView.alpha = 0f

                photoView.scaleType = PhotoView.ScaleType.FIT

                reader = {

                    toScale = photoView.scale

                    toCropArea = getCropAreaByPhotoView()

                }

                animation = {

                    val alpha = 1 - it

                    overlayView.alpha = alpha
                    finderView.alpha = alpha


                    finderView.cropArea = fromCropArea.add(offsetCropArea.multiply(it))

                }

                complete = {
                    photoView.updateLimitScale()
                    foregroundView.alpha = 0f
                }

            }

            photoView.contentInset = toContentInset

            photoView.temp({ baseMatrix, changeMatrix ->
                photoView.resetMatrix(baseMatrix, changeMatrix)
            }, reader)

            finderView.cropArea = fromCropArea

            offsetCropArea = toCropArea.minus(fromCropArea)

            startAnimation(
                photoView.zoomDuration,
                photoView.zoomInterpolator,
                animation,
                complete
            )

            photoView.setFocusPoint(width / 2f, height / 2f)
            photoView.startZoomAnimation(fromScale, toScale)

        }

    private var activeAnimator: ValueAnimator? = null

    private lateinit var configuration: PhotoCropConfiguration

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

        finderView.onCropAreaChange = {
            val rect = finderView.cropArea.toRect(width, height)
            Util.updateView(foregroundView, rect.left, rect.top, rect.width().toInt(), rect.height().toInt())
            Util.updateView(gridView, rect.left, rect.top, rect.width().toInt(), rect.height().toInt())
            foregroundView.updateImageOrigin()
        }
        finderView.onCropAreaResize = {
            updateCropArea(finderView.normalizedCropArea)
        }
        finderView.onInteractionStart = {
            updateInteractionState(configuration.overlayAlphaInteractive, 1f)
        }
        finderView.onInteractionEnd = {
            updateInteractionState(configuration.overlayAlphaNormal, 0f)
        }

        photoView.scaleType = PhotoView.ScaleType.FIT
        photoView.onScaleChange = {
            if (finderView.alpha > 0) {
                updateFinderMinSize()
                if (activeAnimator == null) {
                    finderView.addInteractionTimer()
                }
            }
            if (foregroundView.alpha > 0) {
                foregroundView.updateImageSize()
            }
        }
        photoView.onOriginChange = {
            if (finderView.alpha > 0 && activeAnimator == null) {
                finderView.addInteractionTimer()
            }
            if (foregroundView.alpha > 0) {
                foregroundView.updateImageOrigin()
            }
        }
        photoView.onReset = {
            if (foregroundView.alpha > 0) {
                foregroundView.updateImageSize()
                foregroundView.updateImageOrigin()
            }
        }

        foregroundView.photoView = photoView

    }

    fun init(configuration: PhotoCropConfiguration) {

        this.configuration = configuration

        val density = resources.displayMetrics.density

        finderView.cropRatio = configuration.cropRatio
        finderView.maxWidth = configuration.finderMaxWidth * density
        finderView.maxHeight = configuration.finderMaxHeight * density

    }

    fun setImageUrl(url: String) {
        configuration.loadImage(photoView, url)
        configuration.loadImage(foregroundView.imageView, url)
    }

    fun setImageBitmap(bitmap: Bitmap) {
        photoView.setImageBitmap(bitmap)
        foregroundView.imageView.setImageBitmap(bitmap)
    }

    fun crop(): Bitmap? {

        if (!isCropping) {
            return null
        }

        val drawable = photoView.drawable
        if (drawable !is BitmapDrawable) {
            return null
        }

        foregroundView.save()

        val source = drawable.bitmap
        val sourceWidth = source.width
        val sourceHeight = source.height

        return Bitmap.createBitmap(
            source,
            (Math.abs(foregroundView.relativeX) * sourceWidth).toInt(),
            (Math.abs(foregroundView.relativeY) * sourceHeight).toInt(),
            (foregroundView.relativeWidth * sourceWidth).toInt(),
            (foregroundView.relativeHeight * sourceHeight).toInt()
        )

    }

    private fun startAnimation(duration: Long, interpolator: TimeInterpolator, update: (Float) -> Unit, complete: (() -> Unit)? = null) {

        this.activeAnimator?.cancel()

        val animator = ValueAnimator.ofFloat(0f, 1f)

        animator.duration = duration
        animator.interpolator = interpolator
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

        val density = resources.displayMetrics.density

        finderView.updateMinSize(
            photoView.maxScale / photoView.scale,
            configuration.finderMinWidth * density,
            configuration.finderMinHeight * density
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

        // 设置缩放焦点
        photoView.setFocusPoint(fromRect.centerX(), fromRect.centerY())



        // 获取偏移量
        foregroundView.save()

        finderView.cropArea = toCropArea
        photoView.zoom(toScale / fromScale, true)

        val translate = foregroundView.restore()



        // 开始动画
        finderView.cropArea = fromCropArea
        photoView.zoom(fromScale / toScale, true)

        startAnimation(
            photoView.zoomDuration,
            photoView.zoomInterpolator,
            {
                finderView.cropArea = fromCropArea.add(offsetCropArea.multiply(it))
            }
        )

        photoView.startZoomAnimation(fromScale, toScale)
        photoView.startTranslateAnimation(translate.x, translate.y, photoView.zoomInterpolator)

    }

    private fun updateInteractionState(overlayAlpha: Float, gridAlpha: Float) {

        val overlayFromAlpha = overlayView.alpha
        val overlayOffsetAlpha = overlayAlpha - overlayFromAlpha

        val gridFromAlpha = gridView.alpha
        val gridOffsetAlpha = gridAlpha - gridFromAlpha

        startAnimation(
            500,
            DecelerateInterpolator(),
            {
                overlayView.alpha = overlayFromAlpha + it * overlayOffsetAlpha
                gridView.alpha = gridFromAlpha + it * gridOffsetAlpha
            }
        )

    }

}