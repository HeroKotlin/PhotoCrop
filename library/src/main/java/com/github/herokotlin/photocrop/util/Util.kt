package com.github.herokotlin.photocrop.util

import android.view.View
import android.widget.FrameLayout

internal object Util {

    fun updateView(view: View, x: Int, y: Int, width: Int, height: Int) {
        updateView(view, x, y)
        view.layoutParams = FrameLayout.LayoutParams(width, height)
    }

    fun updateView(view: View, x: Int, y: Int) {
        view.x = x.toFloat()
        view.y = y.toFloat()
    }

    fun showView(view: View) {
        view.visibility = View.VISIBLE
    }

    fun hideView(view: View) {
        view.visibility = View.GONE
    }

}