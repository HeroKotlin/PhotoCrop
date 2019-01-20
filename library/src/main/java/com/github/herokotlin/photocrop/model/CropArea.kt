package com.github.herokotlin.photocrop.model

import android.graphics.Rect

data class CropArea(
    var top: Int,
    val left: Int,
    val bottom: Int,
    val right: Int
) {

    companion object {
        val zero = CropArea(0, 0, 0, 0)
    }

    override fun toString(): String {
        return "top: $top, left: $left, right: $right, bottom: $bottom"
    }

    fun toRect(width: Int, height: Int): Rect {
        return Rect(left, top, width - right, height - bottom)
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
            (top * factor).toInt(),
            (left * factor).toInt(),
            (bottom * factor).toInt(),
            (right * factor).toInt()
        )
    }

}
