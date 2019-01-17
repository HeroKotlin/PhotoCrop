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

    fun toRect(width: Int, height: Int): Rect {
        return Rect(left, top, width - right, height - bottom)
    }

    override fun toString(): String {
        return "(top:$top, left:$left, bottom:$bottom, right:$right)"
    }

}
