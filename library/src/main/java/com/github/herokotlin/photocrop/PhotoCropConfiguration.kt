package com.github.herokotlin.photocrop

import android.widget.ImageView

abstract class PhotoCropConfiguration {

    /**
     * 裁剪宽度，单位是像素，必传
     */
    var cropWidth = 0f

    /**
     * 裁剪高度，单位是像素，必传
     */
    var cropHeight = 0f

    /**
     * 裁剪框最大宽度，0 表示不限制
     */
    var finderMaxWidth = 0

    /**
     * 裁剪框最大高度，0 表示不限制
     */
    var finderMaxHeight = 0

    /**
     * 裁剪框最小宽度
     */
    var finderMinWidth = 60

    /**
     * 裁剪框最小高度
     */
    var finderMinHeight = 60

    /**
     * 不在交互时 Overlay 的透明度
     */
    var overlayAlphaNormal = 1f

    /**
     * 正在交互时 Overlay 的透明度
     */
    var overlayAlphaInteractive = 0.2f

    /**
     * 加载图片
     */
    abstract fun loadImage(imageView: ImageView, url: String)

}