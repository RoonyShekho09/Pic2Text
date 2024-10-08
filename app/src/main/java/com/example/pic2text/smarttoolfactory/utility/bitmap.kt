package com.example.pic2text.smarttoolfactory.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

fun Uri.toBitmap(context: Context): Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, this))
} else {
    MediaStore.Images.Media.getBitmap(context.contentResolver, this)
}