package com.github.herokotlin.photocrop

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.core.view.WindowCompat
import com.github.herokotlin.photocrop.databinding.PhotoCropActivityBinding

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

        // >= 安卓15 关闭 edge-to-edge
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }

        val binding = PhotoCropActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val url = intent.getStringExtra(KEY_URL)

        binding.photoCrop.init(configuration)

        binding.cancelButton.setOnClickListener {
            isCancelClicked = true;
            callback.onCancel(this)
        }

        binding.resetButton.setOnClickListener {
            binding.photoCrop.reset()
        }

        binding.rotateButton.setOnClickListener {
            binding.photoCrop.rotate(-90f)
        }

        binding.photoCrop.onInteractionStart = {
            binding.rotateButton.visibility = View.GONE
        }

        binding.photoCrop.onInteractionEnd = {
            binding.rotateButton.visibility = View.VISIBLE
        }

        binding.submitButton.setOnClickListener {

            val bitmap = binding.photoCrop.crop()
            if (bitmap != null) {
                Thread {
                    binding.photoCrop.save(bitmap)?.let {
                        binding.photoCrop.compress(it)?.let {
                            runOnUiThread {
                                isSubmitClicked = true;
                                callback.onSubmit(this, it)
                            }
                        }
                    }
                }.start()
            }

        }

        if (configuration.guideLabelTitle.isNotEmpty()) {
            binding.guideLabel.text = configuration.guideLabelTitle;
            binding.guideLabel.visibility = View.VISIBLE
        }
        if (configuration.cancelButtonTitle.isNotEmpty()) {
            binding.cancelButton.text = configuration.cancelButtonTitle
        }
        if (configuration.resetButtonTitle.isNotEmpty()) {
            binding.resetButton.text = configuration.resetButtonTitle
        }
        if (configuration.submitButtonTitle.isNotEmpty()) {
            binding.submitButton.text = configuration.submitButtonTitle
        }

        // 外面请求完权限再进来
        url?.let {
            loadImage(this, url) { image ->
                if (image != null) {

                    runOnUiThread {
                        binding.photoCrop.image = image

                        binding.photoCrop.postDelayed({
                            binding.photoCrop.isCropping = true
                            binding.resetButton.visibility = View.VISIBLE
                            binding.submitButton.visibility = View.VISIBLE
                            binding.rotateButton.visibility = View.VISIBLE
                        }, 500)
                    }

                }
            }
        }
    }

    override fun onDestroy() {
        if (!isSubmitClicked && !isCancelClicked) {
            callback.onExit(this)
        }
        super.onDestroy()
    }
}