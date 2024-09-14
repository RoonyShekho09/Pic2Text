package com.example.pic2text

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.os.BuildCompat
import com.example.pic2text.smarttoolfactory.recognizeText.recognizeTextFromImage
import com.example.pic2text.ui.theme.Text2PicTheme
import com.google.modernstorage.photopicker.PhotoPicker
import com.smarttoolfactory.cropper.ImageCropper
import com.smarttoolfactory.cropper.model.OutlineType
import com.smarttoolfactory.cropper.model.RectCropShape
import com.smarttoolfactory.cropper.settings.CropDefaults
import com.smarttoolfactory.cropper.settings.CropOutlineProperty

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Text2PicTheme {
                MainContent {
                    finish()
                }
            }
        }
    }
}

@Composable
fun MainContent(modifier: Modifier = Modifier, finishActivity: () -> Unit) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var croppedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var crop by remember {
        mutableStateOf(false)
    }
    var recognizedText by remember { mutableStateOf("") }

    BackHandler {
        if (imageBitmap != null) {
            imageBitmap = null
        } else {
            finishActivity()
        }
    }
    Scaffold(
        floatingActionButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { crop = true }) {
                    Text(text = "Copy")
                }
                ImageSelectionButton {
                    imageBitmap = it
                }
            }
        }
    ) { paddingValues ->
        imageBitmap?.let {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ImageCropper(
                    crop = crop,
                    imageBitmap = it,
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentDescription = "Image Cropper",
                    cropProperties = CropDefaults.properties(
                        overlayRatio = 0.4f,
                        zoomable = false,
                        cropOutlineProperty = CropOutlineProperty(
                            OutlineType.Rect,
                            RectCropShape(0, "Rect")
                        ),
                        handleSize = 80.dp.value
                    ),
                    onCropSuccess = {
                        crop = false
                        recognizeTextFromImage(it, onTextRecognized = { recognizedText = it })
                        croppedImage = it
                    }
                )
            }
        }
    }


    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotEmpty()) {
            clipboardManager.setText(AnnotatedString(recognizedText))
        }
    }
}


@OptIn(BuildCompat.PrereleaseSdkCheck::class)
@Composable
fun ImageSelectionButton(
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    onImageSelected: (ImageBitmap) -> Unit,
) {
    val context = LocalContext.current

    val photoPicker =
        rememberLauncherForActivityResult(PhotoPicker()) { uris ->
            val uri = uris.firstOrNull() ?: return@rememberLauncherForActivityResult

            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(context.contentResolver, uri)
                ) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }

            onImageSelected(bitmap.asImageBitmap())
        }

    FloatingActionButton(
        elevation = elevation,
        onClick = {
            photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.IMAGES_ONLY, 1))
        },
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null
        )
    }
}
