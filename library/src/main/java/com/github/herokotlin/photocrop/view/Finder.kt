package com.github.herokotlin.photocrop.view

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout

import com.github.herokotlin.photocrop.R
import com.github.herokotlin.photocrop.model.CropArea
import kotlinx.android.synthetic.main.photo_crop_finder.view.*

class Finder: FrameLayout, View.OnTouchListener {

    lateinit var onCropAreaChange: () -> Unit
    lateinit var onCropAreaResize: () -> Unit

    var cropRatio = 1

    var cropArea = CropArea.zero

        set(value) {

            if (field == value) {
                return
            }

            Log.d("photocrop", "$cropArea")
            field = value

            update()
            onCropAreaChange()

        }

    var normalizedCropArea = CropArea.zero

    private var minWidth = 0
    private var minHeight = 0

    private var lastTouchPoint = Point()

    private var resizeCropAreaTimer: Runnable? = null

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

        LayoutInflater.from(context).inflate(R.layout.photo_crop_finder, this)

        topLeftButton.setOnTouchListener(this)
        topRightButton.setOnTouchListener(this)
        bottomLeftButton.setOnTouchListener(this)
        bottomRightButton.setOnTouchListener(this)

        update()

    }

    fun updateMinSize(scaleFactor: Float, minWidth: Int, minHeight: Int) {

        val rect = normalizedCropArea.toRect(width, height)

        this.minWidth = Math.max((rect.width() / scaleFactor).toInt(), minWidth)
        this.minHeight = Math.max((rect.height() / scaleFactor).toInt(), minHeight)

    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {

        if (view == null || event == null) {
            return false
        }

        // 不用用 x 和 y，因为他俩是相对于 view 的
        // 而 view 又在移动中，因此会出现抖动的情况
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                removeResizeCropAreaTimer()
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
                        topLeftButton -> {
                            left = Math.min(right - minWidth, Math.max(maxLeft, left + offsetX))
                            top = bottom - (right - left) / cropRatio
                        }
                        topRightButton -> {
                            right = Math.min(maxRight, Math.max(left + minWidth, right + offsetX))
                            top = bottom - (right - left) / cropRatio
                        }
                        bottomLeftButton -> {
                            left = Math.min(right - minWidth, Math.max(maxLeft, left + offsetX))
                            bottom = top + (right - left) / cropRatio
                        }
                        bottomRightButton -> {
                            right = Math.min(maxRight, Math.max(left + minWidth, right + offsetX))
                            bottom = top + (right - left) / cropRatio
                        }
                    }

                    cropArea = CropArea(top, left, viewHeight - bottom, viewWidth - right)

                }

            }

            MotionEvent.ACTION_UP -> {
                resizeCropAreaTimer = Runnable {
                    resizeCropArea()
                }
                postDelayed(resizeCropAreaTimer, 1000)
            }

        }

        return true

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        super.onSizeChanged(w, h, oldw, oldh)

        val cropWidth = w - cornerButtonSize - 2 * cornerLineWidth
        val cropHeight = cropWidth / cropRatio

        val vertical = (h - cropHeight) / 2
        val horizontal = cornerButtonSize / 2 + cornerLineWidth

        normalizedCropArea = CropArea(vertical, horizontal, vertical, horizontal)

        // 重新计算裁剪区域
        resizeCropArea()

    }

    private fun removeResizeCropAreaTimer() {
        resizeCropAreaTimer?.let {
            removeCallbacks(it)
        }
        resizeCropAreaTimer = null
    }

    private fun resizeCropArea() {
        removeResizeCropAreaTimer()
        onCropAreaResize()
    }

    private fun update() {

        val left = cropArea.left
        val top = cropArea.top
        val right = width - cropArea.right
        val bottom = height - cropArea.bottom

        val halfButtonSize = cornerButtonSize / 2

        updateView(topBorder, left, top - borderWidth, right - left, borderWidth)
        updateView(rightBorder, right, top, borderWidth, bottom - top)
        updateView(bottomBorder, left, bottom, right - left, borderWidth)
        updateView(leftBorder, left - borderWidth, top, borderWidth, bottom - top)

        updateView(topLeftButton, left - cornerLineWidth - halfButtonSize, top - cornerLineWidth - halfButtonSize)
        updateView(topLeftHorizontalLine, left - cornerLineWidth, top - cornerLineWidth)
        updateView(topLeftVerticalLine, left - cornerLineWidth, top - cornerLineWidth)

        updateView(topRightButton, right + cornerLineWidth - halfButtonSize, top - cornerLineWidth - halfButtonSize)
        updateView(topRightHorizontalLine, right + cornerLineWidth - cornerLineSize, top - cornerLineWidth)
        updateView(topRightVerticalLine, right, top - cornerLineWidth)

        updateView(bottomRightButton, right + cornerLineWidth - halfButtonSize, bottom + cornerLineWidth - halfButtonSize)
        updateView(bottomRightHorizontalLine, right + cornerLineWidth - cornerLineSize, bottom)
        updateView(bottomRightVerticalLine, right, bottom + cornerLineWidth - cornerLineSize)

        updateView(bottomLeftButton, left - cornerLineWidth - halfButtonSize, bottom + cornerLineWidth - halfButtonSize)
        updateView(bottomLeftHorizontalLine, left - cornerLineWidth, bottom)
        updateView(bottomLeftVerticalLine, left - cornerLineWidth, bottom + cornerLineWidth - cornerLineSize)

    }

    private fun updateView(view: View, x: Int, y: Int, width: Int, height: Int) {
        updateView(view, x, y)
        view.layoutParams = LayoutParams(width, height)
    }

    private fun updateView(view: View, x: Int, y: Int) {
        view.x = x.toFloat()
        view.y = y.toFloat()
    }

}