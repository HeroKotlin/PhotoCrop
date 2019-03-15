package com.github.herokotlin.photocrop

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.view.View
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

                photoCrop.image = image

                photoCrop.postDelayed({
                    photoCrop.isCropping = true
                    resetButton.visibility = View.VISIBLE
                    submitButton.visibility = View.VISIBLE
                    rotateButton.visibility = View.VISIBLE
                }, 500)

            }
        }

        cancelButton.setOnClickListener {
            callback.onCancel(this)
        }

        resetButton.setOnClickListener {
            photoCrop.reset()
        }

        rotateButton.setOnClickListener {
            photoCrop.rotate()
        }

        photoCrop.onInteractionStart = {
            rotateButton.visibility = View.GONE
        }

        photoCrop.onInteractionEnd = {
            rotateButton.visibility = View.VISIBLE
        }

        submitButton.setOnClickListener {

            val bitmap = photoCrop.crop()
            if (bitmap != null) {
                val handler = Handler(Looper.getMainLooper())
                Thread {
                    val file = photoCrop.save(bitmap)
                    val result = photoCrop.compress(file)
                    handler.post {
                        callback.onSubmit(this, result)
                    }
                }.start()
            }

        }

    }

}