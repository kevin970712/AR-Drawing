package com.android.ardrawing

import android.net.Uri
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import coil.compose.AsyncImage

@Composable
fun DrawingImage(
    uri: Uri,
    imageState: ImageState,
    onGesture: (panX: Float, panY: Float, zoom: Float, rotation: Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (!imageState.isLocked) {
                    Modifier.pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotate ->
                            onGesture(pan.x, pan.y, zoom, rotate)
                        }
                    }
                } else Modifier
            )
            .graphicsLayer(
                scaleX = imageState.scale * if (imageState.isFlippedHorizontal) -1f else 1f,
                scaleY = imageState.scale * if (imageState.isFlippedVertical) -1f else 1f,
                rotationZ = imageState.rotation,
                translationX = imageState.offsetX,
                translationY = imageState.offsetY,
                alpha = imageState.opacity
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "Reference Image",
            modifier = Modifier.fillMaxWidth(0.7f)
        )
    }
}