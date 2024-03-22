package com.github.herokotlin.photocrop.util

import android.graphics.Bitmap
import android.graphics.Matrix
import android.view.View
import android.widget.FrameLayout
import com.github.herokotlin.photocrop.model.CropFile
import java.io.File
import java.io.FileOutputStream
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

    private fun getFilePath(dirname: String, extname: String): String {

        // 确保目录存在
        val file = File(dirname)
        if (!file.exists()) {
            file.mkdir()
        }

        var dirName = dirname
        if (!dirName.endsWith("/")) {
            dirName += "/"
        }

        return dirName + UUID.randomUUID().toString() + extname

    }

    fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {

        val matrix = Matrix()
        matrix.setRotate(degrees)

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)

    }

    fun createNewImage(bitmap: Bitmap, width: Int, height: Int): Bitmap {

        return Bitmap.createScaledBitmap(bitmap, width, height, true)

    }

    fun createNewFile(imageDir: String, bitmap: Bitmap, quality: Float): CropFile {

        var extname = ".jpg"
        var format = Bitmap.CompressFormat.JPEG

        if (bitmap.hasAlpha()) {
            extname = ".png"
            format = Bitmap.CompressFormat.PNG
        }

        val path = getFilePath(imageDir, extname)

        val output = FileOutputStream(path)
        bitmap.compress(format, (quality * 100).toInt(), output)
        output.close()

        val file = File(path)

        return CropFile(path, file.length(), bitmap.width, bitmap.height)

    }

}