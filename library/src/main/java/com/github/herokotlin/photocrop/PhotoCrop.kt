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
import android.view.animation.DecelerateInterpolator
import com.github.herokotlin.photocrop.databinding.PhotoCropBinding
import com.github.herokotlin.photocrop.model.CropFile
import com.github.herokotlin.photocrop.util.Compressor
import kotlin.math.abs

class PhotoCrop: FrameLayout {

    lateinit var binding: PhotoCropBinding

    var image: Bitmap? = null

        set(value) {

            if (field == value) {
                return
            }

            field = value

            binding.photoView.setImageBitmap(value)
            binding.foregroundView.binding.imageView.setImageBitmap(value)

        }

    var isCropping = false

        set(value) {

            if (field == value) {
                return
            }

            field = value

            binding.finderView.stopInteraction()

            var fromCropArea = binding.finderView.cropArea
            var toCropArea = fromCropArea

            var toContentInset = PhotoView.ContentInset.zero

            val fromScale = binding.photoView.scale
            var toScale = fromScale

            var offsetCropArea = CropArea.zero

            val reader: () -> Unit
            val animation: (Float) -> Unit
            val complete: () -> Unit

            if (value) {

                binding.foregroundView.alpha = 1f

                binding.photoView.scaleType = PhotoView.ScaleType.FILL

                fromCropArea = getCropAreaByPhotoView()
                toCropArea = binding.finderView.normalizedCropArea

                toContentInset = toCropArea.toContentInset()

                reader = {

                    toScale = binding.photoView.scale

                }

                val overlayFromAlpha = binding.overlayView.alpha
                val overlayOffsetAlpha = configuration.overlayAlphaNormal - overlayFromAlpha


                animation = {

                    binding.overlayView.alpha = overlayFromAlpha + it * overlayOffsetAlpha
                    binding.finderView.alpha = it

                    binding.finderView.cropArea = fromCropArea.add(offsetCropArea.multiply(it))

                }

                complete = {
                    binding.photoView.updateLimitScale()
                }

            }
            else {

                binding.gridView.alpha = 0f

                binding.photoView.scaleType = PhotoView.ScaleType.FIT

                reader = {

                    toScale = binding.photoView.scale

                    toCropArea = getCropAreaByPhotoView()

                }

                animation = {

                    binding.overlayView.alpha = 1 - it
                    binding.finderView.alpha = 1 - it

                    binding.finderView.cropArea = fromCropArea.add(offsetCropArea.multiply(it))

                }

                complete = {
                    binding.photoView.updateLimitScale()
                    binding.foregroundView.alpha = 0f
                }

            }

            binding.photoView.keep {
                binding.photoView.contentInset = toContentInset
            }

            binding.photoView.temp(
                { baseMatrix, changeMatrix ->
                    binding.photoView.resetMatrix(baseMatrix, changeMatrix)
                },
                reader
            )

            binding.finderView.cropArea = fromCropArea

            offsetCropArea = toCropArea.minus(fromCropArea)

            startAnimation(
                binding.photoView.zoomDuration,
                binding.photoView.zoomInterpolator,
                animation,
                complete
            )

            binding.photoView.setFocusPoint(width / 2f, height / 2f)
            binding.photoView.startZoomAnimation(fromScale, toScale)

        }

    var onInteractionStart: (() -> Unit)? = null
    var onInteractionEnd: (() -> Unit)? = null

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

        binding = PhotoCropBinding.inflate(LayoutInflater.from(context), this, true)

        binding.finderView.onCropAreaChange = {
            val rect = binding.finderView.cropArea.toRect(width, height)
            Util.updateView(binding.foregroundView, rect.left, rect.top, rect.width().toInt(), rect.height().toInt())
            Util.updateView(binding.gridView, rect.left, rect.top, rect.width().toInt(), rect.height().toInt())
            binding.foregroundView.updateImageOrigin()
        }
        binding.finderView.onCropAreaResize = {
            updateCropArea(binding.finderView.normalizedCropArea)
        }
        binding.finderView.onInteractionStart = {
            updateInteractionState(configuration.overlayAlphaInteractive, 1f)
            onInteractionStart?.invoke()
        }
        binding.finderView.onInteractionEnd = {
            updateInteractionState(configuration.overlayAlphaNormal, 0f)
            onInteractionEnd?.invoke()
        }

        binding.photoView.scaleType = PhotoView.ScaleType.FIT
        binding.photoView.onScaleChange = {
            if (binding.finderView.alpha > 0) {
                updateFinderMinSize()
            }
            if (binding.foregroundView.alpha > 0) {
                binding.foregroundView.updateImageSize()
            }
        }
        binding.photoView.onOriginChange = {
            if (binding.foregroundView.alpha > 0) {
                binding.foregroundView.updateImageOrigin()
            }
       }
        binding.photoView.onReset = {
            if (binding.foregroundView.alpha > 0) {
                binding.foregroundView.updateImageSize()
                binding.foregroundView.updateImageOrigin()
            }
        }

        binding.foregroundView.photoView = binding.photoView

        binding.photoView.setOnTouchListener { _, _ ->
            if (isCropping && activeAnimator == null) {
                binding.finderView.addInteractionTimer()
            }
            false
        }

    }

    fun init(configuration: PhotoCropConfiguration) {

        this.configuration = configuration

        val density = resources.displayMetrics.density

        binding.finderView.cropRatio = configuration.cropWidth / configuration.cropHeight
        binding.finderView.maxWidth = configuration.finderMaxWidth * density
        binding.finderView.maxHeight = configuration.finderMaxHeight * density

    }

    fun reset() {

        val fromScale = binding.photoView.scale
        var toScale = fromScale

        binding.photoView.temp({ baseMatrix, changeMatrix ->
            binding.photoView.resetMatrix(baseMatrix, changeMatrix)
        }) {
            toScale = binding.photoView.scale
        }

        binding.photoView.setFocusPoint(width / 2f, height / 2f)
        binding.photoView.startZoomAnimation(fromScale, toScale)

    }

    fun rotate(degrees: Float) {

        image?.let {
            image = Util.rotateImage(it, degrees)
        }

    }

    fun crop(): Bitmap? {

        if (!isCropping) {
            return null
        }

        return image?.let {

            binding.foregroundView.save()

            val source = it
            val sourceWidth = source.width
            val sourceHeight = source.height

            val x = abs(binding.foregroundView.relativeX) * sourceWidth
            val y = abs(binding.foregroundView.relativeY) * sourceHeight
            val width = binding.foregroundView.relativeWidth * sourceWidth
            val height = binding.foregroundView.relativeHeight * sourceHeight

            Bitmap.createBitmap(
                source,
                (Math.floor(x.toDouble())).toInt(),
                (Math.floor(y.toDouble())).toInt(),
                (Math.round(width.toDouble())).toInt(),
                (Math.round(height.toDouble())).toInt()
            )
        }

    }

    fun save(bitmap: Bitmap): CropFile? {

        val cacheDir = context.externalCacheDir ?: return null

        return Util.createNewFile(cacheDir.absolutePath, bitmap, 1f)

    }

    fun compress(source: CropFile): CropFile? {

        val cacheDir = context.externalCacheDir ?: return null

        return Compressor(configuration.maxSize, configuration.quality)
            .compress(
                cacheDir.absolutePath,
                source,
                configuration.cropWidth.toInt(),
                configuration.cropHeight.toInt()
            )

    }

    private fun startAnimation(duration: Long, interpolator: TimeInterpolator, update: (Float) -> Unit, complete: (() -> Unit)? = null) {

        activeAnimator?.cancel()

        val animator = ValueAnimator.ofFloat(0f, 1f)

        animator.duration = duration
        animator.interpolator = interpolator
        animator.addUpdateListener {
            update(it.animatedValue as Float)
        }
        animator.addListener(object: AnimatorListenerAdapter() {
            // 动画被取消，onAnimationEnd() 也会被调用
            override fun onAnimationEnd(animation: android.animation.Animator) {
                complete?.invoke()
                if (animation == activeAnimator) {
                    activeAnimator = null
                }
            }
        })
        animator.start()

        activeAnimator = animator

    }

    private fun updateFinderMinSize() {

        val density = resources.displayMetrics.density

        val finderMinWidth = configuration.finderMinWidth * density
        val finderMinHeight = configuration.finderMinHeight * density

        // 有两个限制：
        // 1. 裁剪框不能小于 finderMinWidth/finderMinHeight
        // 2. 裁剪后的图片不能小余 cropWidth/cropHeight

        val normalizedRect = binding.finderView.normalizedCropArea.toRect(width, height)
        val normalizedWidth = normalizedRect.width()
        val normalizedHeight = normalizedRect.height()

        // 这是裁剪框能缩放的最小尺寸
        val scaleFactor = binding.photoView.maxScale / binding.photoView.scale
        val finderWidth = Math.max(normalizedWidth / scaleFactor, finderMinWidth)
        val finderHeight = Math.max(normalizedHeight / scaleFactor, finderMinHeight)

        // 裁剪框尺寸对应的图片尺寸
        // 因为 photoView 已到达 maxScale，因此裁剪框和图片是 1:1 的关系
//        val cropWidth = configuration.cropWidth
//        val cropHeight = configuration.cropHeight
//        if (finderWidth < cropWidth) {
//            finderWidth = cropWidth / scaleFactor
//        }
//        if (finderHeight < cropHeight) {
//            finderHeight = cropHeight / scaleFactor
//        }

        binding.finderView.minWidth = finderWidth
        binding.finderView.minHeight = finderHeight

    }

    // CropArea 完全覆盖 PhotoView
    private fun getCropAreaByPhotoView(): CropArea {

        val imageOrigin = binding.photoView.imageOrigin
        val imageSize = binding.photoView.imageSize

        val left = Math.max(imageOrigin.x, 0f)
        val top = Math.max(imageOrigin.y, 0f)

        val right = Math.max(binding.photoView.width - (imageOrigin.x + imageSize.width), 0f)
        val bottom = Math.max(binding.photoView.height - (imageOrigin.y + imageSize.height), 0f)

        return CropArea(top, left, bottom, right)

    }

    private fun updateCropArea(cropArea: CropArea) {

        val fromCropArea = binding.finderView.cropArea
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

        val fromScale = binding.photoView.scale
        val toScale = fromScale * scale

        // 设置缩放焦点
        binding.photoView.setFocusPoint(fromRect.centerX(), fromRect.centerY())



        // 获取偏移量
        binding.foregroundView.save()

        binding.finderView.cropArea = toCropArea
        binding.photoView.zoom(toScale / fromScale, true)

        val translate = binding.foregroundView.restore()



        // 开始动画
        binding.finderView.cropArea = fromCropArea
        binding.photoView.zoom(fromScale / toScale, true)

        startAnimation(
            binding.photoView.zoomDuration,
            binding.photoView.zoomInterpolator,
            {
                binding.finderView.cropArea = fromCropArea.add(offsetCropArea.multiply(it))
            }
        )

        binding.photoView.startZoomAnimation(fromScale, toScale)
        binding.photoView.startTranslateAnimation(translate.x, translate.y, binding.photoView.zoomInterpolator)

    }

    private fun updateInteractionState(overlayAlpha: Float, gridAlpha: Float) {

        val overlayFromAlpha = binding.overlayView.alpha
        val overlayOffsetAlpha = overlayAlpha - overlayFromAlpha

        val gridFromAlpha = binding.gridView.alpha
        val gridOffsetAlpha = gridAlpha - gridFromAlpha

        startAnimation(
            500,
            DecelerateInterpolator(),
            {
                binding.overlayView.alpha = overlayFromAlpha + it * overlayOffsetAlpha
                binding.gridView.alpha = gridFromAlpha + it * gridOffsetAlpha
            }
        )

    }

}