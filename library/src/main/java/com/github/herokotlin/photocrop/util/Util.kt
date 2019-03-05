package com.github.herokotlin.photocrop.util

import android.graphics.Bitmap
import android.view.View
import android.widget.FrameLayout
import com.github.herokotlin.photocrop.model.CropFile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

internal object Util {

    private var index = 0

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

    private fun getFilePath(dirname: String, extname: String): String {

        // 确保目录存在
        val file = File(dirname)
        if (!file.exists()) {
            file.mkdir()
        }

        // 时间格式的文件名
        val formater = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)

        // 避免相同时间执行多次
        index += 1

        val filename = "${formater.format(Date())}_$index$extname"

        if (dirname.endsWith("/")) {
            return dirname + filename
        }

        return "$dirname/$filename"

    }

    fun createNewImage(bitmap: Bitmap, width: Int, height: Int): Bitmap {

        return Bitmap.createScaledBitmap(bitmap, width, height, true)

    }

    fun createNewFile(imageDir: String, bitmap: Bitmap, quality: Float): CropFile {

        val path = Util.getFilePath(imageDir, ".jpg")

        val output = FileOutputStream(path)
        bitmap.compress(Bitmap.CompressFormat.JPEG, (quality * 100).toInt(), output)
        output.close()

        val file = File(path)

        return CropFile(path, file.length(), bitmap.width, bitmap.height)

    }

}