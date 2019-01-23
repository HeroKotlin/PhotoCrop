package com.github.herokotlin.photocrop.util

import android.view.View
import android.widget.FrameLayout
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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

    fun getFilePath(dirname: String, extname: String): String {

        // 确保目录存在
        val file = File(dirname)
        if (!file.exists()) {
            file.mkdir()
        }

        // 时间格式的文件名
        val formater = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)

        val filename = "${formater.format(Date())}$extname"

        if (dirname.endsWith("/")) {
            return dirname + filename
        }

        return "$dirname/$filename"

    }

}