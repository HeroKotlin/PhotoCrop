package com.github.herokotlin.photocrop.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.github.herokotlin.photocrop.R

internal class OverlayView: View {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        setBackgroundResource(R.color.photo_crop_overlay_color)
    }

}