package com.github.herokotlin.photocrop.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.github.herokotlin.photocrop.model.CropFile
import java.lang.Exception

class Compressor {

    var maxWidth = 3000

    var maxHeight = 3000

    var maxSize = 200 * 1024

    var quality = 0.5f

    constructor(maxWidth: Int, maxHeight: Int, maxSize: Int, quality: Float) {
        this.maxWidth = maxWidth
        this.maxHeight = maxHeight
        this.maxSize = maxSize
        this.quality = quality
    }

    constructor(maxSize: Int, quality: Float) {
        this.maxSize = maxSize
        this.quality = quality
    }

    fun compress(imageDir: String, source: CropFile): CropFile {

        if (source.size < maxSize) {
            return source
        }

        val bitmap: Bitmap
        try {
            bitmap = BitmapFactory.decodeFile(source.path)
        }
        catch (ex: Throwable) {
            ex.printStackTrace()
            return source
        }

        val lowQuality = Util.createNewFile(imageDir, bitmap, quality)
        if (lowQuality.size < maxSize) {
            return lowQuality
        }

        var width = source.width
        var height = source.height

        val ratio = if (height > 0) width.toFloat() / height else 1f

        if (width > maxWidth && height > maxHeight) {
            // 看短边
            if (width / maxWidth > height / maxHeight) {
                height = maxHeight
                width = (height * ratio).toInt()
            }
            else {
                width = maxWidth
                height = (width / ratio).toInt()
            }
        }
        else if (width > maxWidth && height <= maxHeight) {
            width = maxWidth
            height = (width / ratio).toInt()
        }
        else if (width <= maxWidth && height > maxHeight) {
            height = maxHeight
            width = (height * ratio).toInt()
        }

        if (width != source.width || height != source.height) {
            return compress(imageDir, bitmap, width, height)
        }

        return lowQuality

    }

    fun compress(imageDir: String, source: CropFile, width: Int, height: Int): CropFile {

        if (source.width == width && source.height == height && source.size < maxSize) {
            return source
        }

        val bitmap: Bitmap
        try {
            bitmap = BitmapFactory.decodeFile(source.path)
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            return source
        }

        return compress(imageDir, bitmap, width, height)

    }

    private fun compress(imageDir: String, bitmap: Bitmap, width: Int, height: Int): CropFile {

        val scaledBitmap = Util.createNewImage(bitmap, width, height)

        val file = Util.createNewFile(imageDir, scaledBitmap, 1f)
        if (file.size < maxSize) {
            return file
        }

        return Util.createNewFile(imageDir, scaledBitmap, quality)

    }


}