package com.github.herokotlin.photocrop.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.github.herokotlin.photocrop.R
import com.github.herokotlin.photocrop.databinding.PhotoCropGridBinding

internal class GridView: FrameLayout {

    lateinit var binding: PhotoCropGridBinding

    private val lineWidth: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.photo_crop_grid_line_width)
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
        binding = PhotoCropGridBinding.inflate(LayoutInflater.from(context), this)
        update()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        update()
    }

    private fun update() {

        if (width == 0 || height == 0) {
            return
        }

        val horizontalLines = listOf(binding.horizontalLine1, binding.horizontalLine2)
        val verticalLines = listOf(binding.verticalLine1, binding.verticalLine2)

        val rowSpacing = height / (horizontalLines.count() + 1)
        val columnSpacing = width / (verticalLines.count() + 1)

        horizontalLines.forEachIndexed { index, view ->
            val offset = rowSpacing * (index + 1) + lineWidth * index
            view.x = 0f
            view.y = offset.toFloat()
        }

        verticalLines.forEachIndexed { index, view ->
            val offset = columnSpacing * (index + 1) + lineWidth * index
            view.x = offset.toFloat()
            view.y = 0f
        }

    }

}