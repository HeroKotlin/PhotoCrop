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

        // 是否需要缩放
        var scaled = false

        if (width > maxWidth && height > maxHeight) {
            scaled = true
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
            scaled = true
            width = maxWidth
            height = width / ratio
        }
        else if (width <= maxWidth && height > maxHeight) {
            scaled = true
            height = maxHeight
            width = height * ratio
        }


        val options = BitmapFactory.Options()

        // 如果值大于 1，在解码过程中将按比例返回占更小内存的 Bitmap。
        // 例如值为 2，则对宽高进行缩放一半。
        options.inSampleSize = Util.getInSampleSize(source.width, source.height, width, height)
        options.inTempStorage = ByteArray(16 * 1024)

        val bitmap: Bitmap
        try {
            bitmap = BitmapFactory.decodeFile(source.path, options)
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            return source
        }

        val file = Util.createNewFile(context, bitmap, 1f)
        if (file.size < maxSize) {
            return file
        }

        return Util.createNewFile(context, bitmap, quality)

    }

    fun compress(context: Context, source: CropFile, width: Int, height: Int): CropFile {

        if (source.width == width && source.height == height) {
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