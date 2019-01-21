package com.github.herokotlin.photocrop.util

import android.view.View
import android.widget.FrameLayout

internal object Util {

    fun updateView(view: View, x: Float, y: Float, width: Int, height: Int) {
        updateOrigin(view, x, y)
        updateSize(view, width, height)
    }

    fun updateOrigin(view: View, x: Float, y: Float) {
        view.x = x
        view.y = y
    }

    fun updateSize(view: View, width: Int, height: Int) {
        view.layoutParams = FrameLayout.LayoutParams(width, height)
    }

    fun isVisible(view: View): Boolean {
        return view.alpha > 0
    }

}