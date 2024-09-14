package com.example.pic2text.smarttoolfactory.recognizeText

import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


fun recognizeTextFromImage(uri: ImageBitmap, onTextRecognized: (String) -> Unit) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val image = InputImage.fromBitmap(uri.asAndroidBitmap(), 0)

    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            onTextRecognized(visionText.text)
        }
        .addOnFailureListener { e ->
            Log.d("ZZZZZZZ", e.message.toString())
        }
}