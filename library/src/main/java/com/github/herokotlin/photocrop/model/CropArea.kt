package com.github.herokotlin.photocrop.model

import android.graphics.RectF

data class CropArea(
    var top: Float,
    val left: Float,
    val bottom: Float,
    val right: Float
) {

    companion object {
        val zero = CropArea(0f, 0f, 0f, 0f)
    }

    override fun toString(): String {
        return "top: $top, left: $left, right: $right, bottom: $bottom"
    }

    fun toRect(width: Int, height: Int): RectF {
        return RectF(left, top, width - right, height - bottom)
    }

    fun add(cropArea: CropArea): CropArea {
        return CropArea(
            top + cropArea.top,
            left + cropArea.left,
            bottom + cropArea.bottom,
            right + cropArea.right
        )
    }

    fun minus(cropArea: CropArea): CropArea {
        return CropArea(
            top - cropArea.top,
            left - cropArea.left,
            bottom - cropArea.bottom,
            right - cropArea.right
        )
    }

    fun multiply(factor: Float): CropArea {
        return CropArea(
            top * factor,
            left * factor,
            bottom * factor,
            right * factor
        )
    }

}
