package com.github.herokotlin.photocrop.view

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout

import com.github.herokotlin.photocrop.R
import com.github.herokotlin.photocrop.databinding.PhotoCropFinderBinding
import com.github.herokotlin.photocrop.model.CropArea
import com.github.herokotlin.photocrop.util.Util

internal class FinderView: FrameLayout, View.OnTouchListener {

    lateinit var binding: PhotoCropFinderBinding

    lateinit var onCropAreaChange: () -> Unit
    lateinit var onCropAreaResize: () -> Unit

    lateinit var onInteractionStart: () -> Unit
    lateinit var onInteractionEnd: () -> Unit

    var cropRatio = 1f

    var minWidth = 0f
    var minHeight = 0f

    var maxWidth = 0f
    var maxHeight = 0f

    var cropArea = CropArea.zero

        set(value) {

            if (field == value) {
                return
            }

            field = value

            update()
            onCropAreaChange()

        }

    var normalizedCropArea = CropArea.zero

    private var lastTouchPoint = Point()

    private var resizeCropAreaTimer: Runnable? = null

    private var interactionTimer: Runnable? = null

    private var isInteractive = false

        set(value) {

            if (!Util.isVisible(this)) {
                return
            }

            if (field == value) {
                return
            }

            field = value

            if (value) {
                onInteractionStart()
            }
            else {
                onInteractionEnd()
            }

        }

    private val borderWidth: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.photo_crop_finder_border_width)
    }

    private val cornerLineWidth: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.photo_crop_finder_corner_line_width)
    }

    private val cornerLineSize: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.photo_crop_finder_corner_line_size)
    }

    private val cornerButtonSize: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.photo_crop_finder_corner_button_size)
    }

    private val touchSlop: Float by lazy {
        ViewConfiguration.get(context).scaledTouchSlop.toFloat()
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

        binding = PhotoCropFinderBinding.inflate(LayoutInflater.from(context), this)

        binding.topLeftButton.setOnTouchListener(this)
        binding.topRightButton.setOnTouchListener(this)
        binding.bottomLeftButton.setOnTouchListener(this)
        binding.bottomRightButton.setOnTouchListener(this)

        update()

    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {

        if (view == null || event == null || !Util.isVisible(this)) {
            return false
        }

        // 不用用 x 和 y，因为他俩是相对于 view 的
        // 而 view 又在移动中，因此会出现抖动的情况
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {

                if (resizeCropAreaTimer != null
                    || (view != binding.topLeftButton && view != binding.topRightButton && view != binding.bottomLeftButton && view != binding.bottomRightButton)
                ) {
                    return false
                }

                lastTouchPoint.x = x
                lastTouchPoint.y = y

            }

            MotionEvent.ACTION_MOVE -> {

                val offsetX = x - lastTouchPoint.x
                val offsetY = y - lastTouchPoint.y

                val distance = Math.sqrt((offsetX * offsetX + offsetY * offsetY).toDouble())
                if (distance > touchSlop) {

                    lastTouchPoint.x = x
                    lastTouchPoint.y = y

                    val viewWidth = width
                    val viewHeight = height

                    var left = cropArea.left
                    var top = cropArea.top

                    var right = viewWidth - cropArea.right
                    var bottom = viewHeight - cropArea.bottom

                    val maxLeft = normalizedCropArea.left
                    val maxRight = viewWidth - normalizedCropArea.right

                    when (view) {
                        binding.topLeftButton -> {
                            left = Math.min(right - minWidth, Math.max(maxLeft, left + offsetX))
                            top = bottom - (right - left) / cropRatio
                        }
                        binding.topRightButton -> {
                            right = Math.min(maxRight, Math.max(left + minWidth, right + offsetX))
                            top = bottom - (right - left) / cropRatio
                        }
                        binding.bottomLeftButton -> {
                            left = Math.min(right - minWidth, Math.max(maxLeft, left + offsetX))
                            bottom = top + (right - left) / cropRatio
                        }
                        binding.bottomRightButton -> {
                            right = Math.min(maxRight, Math.max(left + minWidth, right + offsetX))
                            bottom = top + (right - left) / cropRatio
                        }
                    }

                    cropArea = CropArea(top, left, viewHeight - bottom, viewWidth - right)

                    addInteractionTimer()
                    addResizeCropAreaTimer()
                }

            }

        }

        return true

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        super.onSizeChanged(w, h, oldw, oldh)

        var cropWidth = w.toFloat() - cornerButtonSize - 2 * cornerLineWidth
        var cropHeight = cropWidth / cropRatio

        if (cropHeight > h) {
            cropHeight = h.toFloat() - cornerButtonSize - 2 * cornerLineWidth
            cropWidth = cropHeight * cropRatio
        }

        if (maxWidth > 0 && cropWidth > maxWidth) {
            cropWidth = maxWidth
            cropHeight = cropWidth / cropRatio
        }

        if (maxHeight > 0 && cropHeight > maxHeight) {
            cropHeight = maxHeight
            cropWidth = cropHeight * cropRatio
        }

        val vertical = (h - cropHeight) / 2f
        val horizontal = (w - cropWidth) / 2f

        normalizedCropArea = CropArea(vertical, horizontal, vertical, horizontal)

        // 重新计算裁剪区域
        if (oldw > 0 && oldh > 0) {
            resizeCropArea()
        }
        else {
            update()
        }

    }

    private fun addResizeCropAreaTimer() {
        removeResizeCropAreaTimer()
        resizeCropAreaTimer = Runnable {
            resizeCropArea()
        }
        postDelayed(resizeCropAreaTimer, 500)
    }

    private fun removeResizeCropAreaTimer() {
        resizeCropAreaTimer?.let {
            removeCallbacks(it)
        }
        resizeCropAreaTimer = null
    }

    fun addInteractionTimer() {
        removeInteractionTimer()
        interactionTimer = Runnable {
            stopInteraction()
        }
        postDelayed(interactionTimer, 1000)
        isInteractive = true
    }

    private fun removeInteractionTimer() {
        interactionTimer?.let {
            removeCallbacks(it)
        }
        interactionTimer = null
    }

    private fun resizeCropArea() {
        removeResizeCropAreaTimer()
        if (Util.isVisible(this)) {
            onCropAreaResize()
        }
    }

    fun stopInteraction() {
        removeInteractionTimer()
        isInteractive = false
    }

    private fun update() {

        if (width == 0 || height == 0) {
            return
        }

        val left = cropArea.left
        val top = cropArea.top
        val right = width - cropArea.right
        val bottom = height - cropArea.bottom

        val halfButtonSize = cornerButtonSize / 2

        Util.updateView(binding.topBorder, left, top - borderWidth, (right - left).toInt(), borderWidth)
        Util.updateView(binding.rightBorder, right, top, borderWidth, (bottom - top).toInt())
        Util.updateView(binding.bottomBorder, left, bottom, (right - left).toInt(), borderWidth)
        Util.updateView(binding.leftBorder, left - borderWidth, top, borderWidth, (bottom - top).toInt())

        Util.updateOrigin(binding.topLeftButton, left - cornerLineWidth - halfButtonSize, top - cornerLineWidth - halfButtonSize)
        Util.updateOrigin(binding.topLeftHorizontalLine, left - cornerLineWidth, top - cornerLineWidth)
        Util.updateOrigin(binding.topLeftVerticalLine, left - cornerLineWidth, top - cornerLineWidth)

        Util.updateOrigin(binding.topRightButton, right + cornerLineWidth - halfButtonSize, top - cornerLineWidth - halfButtonSize)
        Util.updateOrigin(binding.topRightHorizontalLine, right + cornerLineWidth - cornerLineSize, top - cornerLineWidth)
        Util.updateOrigin(binding.topRightVerticalLine, right, top - cornerLineWidth)

        Util.updateOrigin(binding.bottomRightButton, right + cornerLineWidth - halfButtonSize, bottom + cornerLineWidth - halfButtonSize)
        Util.updateOrigin(binding.bottomRightHorizontalLine, right + cornerLineWidth - cornerLineSize, bottom)
        Util.updateOrigin(binding.bottomRightVerticalLine, right, bottom + cornerLineWidth - cornerLineSize)

        Util.updateOrigin(binding.bottomLeftButton, left - cornerLineWidth - halfButtonSize, bottom + cornerLineWidth - halfButtonSize)
        Util.updateOrigin(binding.bottomLeftHorizontalLine, left - cornerLineWidth, bottom)
        Util.updateOrigin(binding.bottomLeftVerticalLine, left - cornerLineWidth, bottom + cornerLineWidth - cornerLineSize)

    }

}