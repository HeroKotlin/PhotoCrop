package com.github.herokotlin.photocrop

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.github.herokotlin.photocrop.util.Util
import kotlinx.android.synthetic.main.photo_crop_activity.*

class PhotoCropActivity: AppCompatActivity() {

    companion object {

        lateinit var callback: PhotoCropCallback
        lateinit var configuration: PhotoCropConfiguration

        lateinit var loadImage: (Context, String, (Bitmap?) -> Unit) -> Unit

        private const val KEY_URL = "url"

        fun newInstance(context: Context, url: String) {
            val intent = Intent(context, PhotoCropActivity::class.java)
            intent.putExtra(KEY_URL, url)
            context.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        var flags = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags = flags or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }

        window.decorView.systemUiVisibility = flags

        setContentView(R.layout.photo_crop_activity)

        val url = intent.getStringExtra(KEY_URL)

        photoCrop.init(configuration)

        loadImage(this, url) { image ->
            if (image != null) {

                // 为了避免内存爆炸，加个最大尺寸限制
                // 毕竟裁剪也不需要非常大的图，通常就是个头像封面啥的，限制到 3000 足够用了
                val maxSize = 3000

                var width = image.width
                var height = image.height
                val ratio = width.toFloat() / height

                // 压缩图片
                if (width > maxSize) {
                    width = maxSize
                    height = (width / ratio).toInt()
                }
                if (height > maxSize) {
                    height = maxSize
                    width = (height * ratio).toInt()
                }

                if (width != image.width || height != image.height) {
                    photoCrop.image = Util.createNewImage(image, width, height)
                }
                else {
                    photoCrop.image = image
                }

                photoCrop.postDelayed({
                    photoCrop.isCropping = true
                    resetButton.visibility = View.VISIBLE
                    submitButton.visibility = View.VISIBLE
                }, 500)

            }
        }

        cancelButton.setOnClickListener {
            callback.onCancel(this)
        }

        resetButton.setOnClickListener {
            photoCrop.reset()
        }

        submitButton.setOnClickListener {
            val bitmap = photoCrop.crop()
            if (bitmap != null) {
                val file = photoCrop.save(bitmap)
                val result = photoCrop.compress(file)
                callback.onSubmit(this, result)
            }
        }

    }

}