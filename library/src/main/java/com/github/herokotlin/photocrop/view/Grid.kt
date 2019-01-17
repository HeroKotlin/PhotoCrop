package com.github.herokotlin.photocrop.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.github.herokotlin.photocrop.R
import kotlinx.android.synthetic.main.photo_crop_grid.view.*

class Grid: FrameLayout {

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
        LayoutInflater.from(context).inflate(R.layout.photo_crop_grid, this)
        update()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        update()
    }

    private fun update() {

        val horizontalLines = listOf(horizontalLine1, horizontalLine2)
        val verticalLines = listOf(verticalLine1, verticalLine2)

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