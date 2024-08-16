package com.github.herokotlin.photocrop

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
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

    private var isSubmitClicked = false
    private var isCancelClicked = false

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

        cancelButton.setOnClickListener {
            isCancelClicked = true;
            callback.onCancel(this)
        }

        resetButton.setOnClickListener {
            photoCrop.reset()
        }

        rotateButton.setOnClickListener {
            photoCrop.rotate(-90f)
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
                    photoCrop.save(bitmap)?.let {
                        photoCrop.compress(it)?.let {
                            handler.post {
                                isSubmitClicked = true;
                                callback.onSubmit(this, it)
                            }
                        }
                    }
                }.start()
            }

        }

        if (configuration.guideLabelTitle.isNotEmpty()) {
            guideLabel.text = configuration.guideLabelTitle;
            guideLabel.visibility = View.VISIBLE
        }
        if (configuration.cancelButtonTitle.isNotEmpty()) {
            cancelButton.text = configuration.cancelButtonTitle
        }
        if (configuration.resetButtonTitle.isNotEmpty()) {
            resetButton.text = configuration.resetButtonTitle
        }
        if (configuration.submitButtonTitle.isNotEmpty()) {
            submitButton.text = configuration.submitButtonTitle
        }

        // 外面请求完权限再进来
        loadImage(this, url) { image ->
            if (image != null) {

                // 回到主线程
                photoCrop.post {

                    photoCrop.image = image

                    photoCrop.postDelayed({
                        photoCrop.isCropping = true
                        resetButton.visibility = View.VISIBLE
                        submitButton.visibility = View.VISIBLE
                        rotateButton.visibility = View.VISIBLE
                    }, 500)

                }

            }
        }

    }

    override fun onDestroy() {
        if (!isSubmitClicked && !isCancelClicked) {
            callback.onCancel(this)
        }
        super.onDestroy()
    }
}