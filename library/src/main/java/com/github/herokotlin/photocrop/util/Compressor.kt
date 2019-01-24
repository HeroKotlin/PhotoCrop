package com.github.herokotlin.photocrop.util

import android.content.Context
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

    fun compress(context: Context, source: CropFile): CropFile {

        if (source.size < maxSize) {
            return source
        }

        var width = source.width
        var height = source.height

        val ratio = if (height > 0) width / height else 1

        if (width > maxWidth && height > maxHeight) {
            // 看短边
            if (width / maxWidth > height / maxHeight) {
                height = maxHeight
                width = height * ratio
            }
            else {
                width = maxWidth
                height = width / ratio
            }
        }
        else if (width > maxWidth && height <= maxHeight) {
            width = maxWidth
            height = width / ratio
        }
        else if (width <= maxWidth && height > maxHeight) {
            height = maxHeight
            width = height * ratio
        }

        return compress(context, source, width, height)

    }

    fun compress(context: Context, source: CropFile, width: Int, height: Int): CropFile {

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

        val scaledBitmap = Util.createNewImage(bitmap, width, height)

        val file = Util.createNewFile(context, scaledBitmap, 1f)
        if (file.size < maxSize) {
            return file
        }

        return Util.createNewFile(context, scaledBitmap, quality)

    }


}