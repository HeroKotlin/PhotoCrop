package com.github.herokotlin.photocrop.model

data class CropFile(
    val path: String,
    val size: Long,
    val width: Int,
    val height: Int
)