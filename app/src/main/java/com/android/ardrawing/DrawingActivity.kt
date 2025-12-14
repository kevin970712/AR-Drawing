package com.android.ardrawing

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.ardrawing.ui.theme.ARDrawingTheme

class DrawingActivity : ComponentActivity() {
    private val viewModel: DrawingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val imageUri = intent.data
        if (imageUri == null) {
            finish()
            return
        }

        setContent {
            ARDrawingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DrawingScreen(
                        selectedUri = imageUri,
                        viewModel = viewModel,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawingScreen(
    selectedUri: android.net.Uri,
    viewModel: DrawingViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    // 從 ViewModel 收集狀態
    val imageState by viewModel.imageState.collectAsStateWithLifecycle()
    val isCameraOn by viewModel.isCameraOn.collectAsStateWithLifecycle()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsStateWithLifecycle()
    val activePanel by viewModel.activePanel.collectAsStateWithLifecycle()
    val isKeepScreenOn by viewModel.isKeepScreenOn.collectAsStateWithLifecycle()

    DisposableEffect(isKeepScreenOn) {
        val window = (context as? Activity)?.window
        if (isKeepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            // 當離開此畫面時，記得清除設定，以免影響其他頁面
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // --- 相機與繪圖層 ---
        if (isCameraOn) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                isFlashlightOn = isFlashlightOn
            )

            DrawingImage(
                uri = selectedUri,
                imageState = imageState,
                onGesture = viewModel::onTransformGesture
            )
        } else {
            // 暫停畫面
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.VideocamOff,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.camera_paused),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }

        // --- 頂部導航列 ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 4.dp
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                onClick = {
                    try {
                        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        context.startActivity(captureIntent)
                    }
                },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 4.dp
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Icon(
                        Icons.Rounded.CameraAlt,
                        "System Camera",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // --- 底部控制面板 ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            // 滑桿與功能面板
            AnimatedVisibility(
                visible = activePanel != DrawingViewModel.ControlPanel.NONE,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (activePanel) {
                        DrawingViewModel.ControlPanel.OPACITY -> {
                            Text(
                                text = stringResource(
                                    R.string.format_opacity,
                                    (imageState.opacity * 100).toInt()
                                ),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Slider(
                                value = imageState.opacity,
                                onValueChange = viewModel::updateOpacity,
                                valueRange = 0.1f..1f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        DrawingViewModel.ControlPanel.SCALE -> {
                            Text(
                                text = stringResource(
                                    R.string.format_size,
                                    (imageState.scale * 100).toInt()
                                ),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Slider(
                                value = imageState.scale,
                                onValueChange = viewModel::updateScale,
                                valueRange = 0.1f..5f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        // 翻轉面板內容
                        DrawingViewModel.ControlPanel.FLIP -> {
                            Text(
                                text = stringResource(R.string.label_flip),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // 水平翻轉按鈕
                                ControlIcon(
                                    icon = Icons.Rounded.SwapHoriz,
                                    label = stringResource(R.string.label_flip_h),
                                    isActive = imageState.isFlippedHorizontal,
                                    onClick = viewModel::toggleFlipHorizontal
                                )
                                // 垂直翻轉按鈕
                                ControlIcon(
                                    icon = Icons.Rounded.SwapVert,
                                    label = stringResource(R.string.label_flip_v),
                                    isActive = imageState.isFlippedVertical,
                                    onClick = viewModel::toggleFlipVertical
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }

            // 底部按鈕列
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(vertical = 18.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 相機開關
                    ControlIcon(
                        icon = if (isCameraOn) Icons.Rounded.Videocam else Icons.Rounded.VideocamOff,
                        label = if (isCameraOn) stringResource(R.string.label_pause) else stringResource(
                            R.string.label_resume
                        ),
                        isActive = isCameraOn,
                        onClick = viewModel::toggleCamera
                    )

                    // 透明度
                    ControlIcon(
                        icon = Icons.Rounded.Opacity,
                        label = stringResource(R.string.label_opacity),
                        isActive = activePanel == DrawingViewModel.ControlPanel.OPACITY,
                        onClick = { viewModel.togglePanel(DrawingViewModel.ControlPanel.OPACITY) },
                        enabled = isCameraOn
                    )

                    // 大小
                    ControlIcon(
                        icon = Icons.Rounded.ZoomIn,
                        label = stringResource(R.string.label_size),
                        isActive = activePanel == DrawingViewModel.ControlPanel.SCALE,
                        onClick = { viewModel.togglePanel(DrawingViewModel.ControlPanel.SCALE) },
                        enabled = isCameraOn
                    )

                    // 翻轉
                    ControlIcon(
                        icon = Icons.Rounded.Flip, // 需要 material-icons-extended 依賴
                        label = stringResource(R.string.label_flip),
                        isActive = activePanel == DrawingViewModel.ControlPanel.FLIP,
                        onClick = { viewModel.togglePanel(DrawingViewModel.ControlPanel.FLIP) },
                        enabled = isCameraOn
                    )

                    // 閃光燈
                    ControlIcon(
                        icon = if (isFlashlightOn) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                        label = stringResource(R.string.label_flash),
                        isActive = isFlashlightOn,
                        activeColor = Color(0xFFFFD700),
                        onClick = viewModel::toggleFlashlight,
                        enabled = isCameraOn
                    )

                    // 螢幕常亮
                    ControlIcon(
                        icon = Icons.Rounded.Smartphone,
                        label = stringResource(R.string.label_screen_on),
                        isActive = isKeepScreenOn,
                        activeColor = MaterialTheme.colorScheme.primary,
                        onClick = viewModel::toggleKeepScreenOn,
                        enabled = isCameraOn
                    )

                    // 鎖定
                    ControlIcon(
                        icon = if (imageState.isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                        label = if (imageState.isLocked) stringResource(R.string.label_locked) else stringResource(
                            R.string.label_lock
                        ),
                        isActive = imageState.isLocked,
                        activeColor = MaterialTheme.colorScheme.error,
                        onClick = viewModel::toggleLock,
                        enabled = isCameraOn
                    )

                    // 還原
                    ControlIcon(
                        icon = Icons.Rounded.Refresh,
                        label = stringResource(R.string.label_reset),
                        isActive = false,
                        onClick = viewModel::resetImage,
                        enabled = isCameraOn
                    )
                }
            }
        }
    }
}

@Composable
fun ControlIcon(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val tintColor = if (!enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    } else if (isActive) {
        activeColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                enabled = enabled
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = tintColor,
            maxLines = 1
        )
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    isFlashlightOn: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context.applicationContext)
    }
    var camera by remember { mutableStateOf<Camera?>(null) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx.applicationContext)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", exc)
                }
            }, executor)
            previewView
        },
        modifier = modifier
    )

    LaunchedEffect(isFlashlightOn, camera) {
        if (camera != null) {
            try {
                if (camera!!.cameraInfo.hasFlashUnit()) {
                    camera!!.cameraControl.enableTorch(isFlashlightOn)
                }
            } catch (e: Exception) {
                Log.e("CameraPreview", "Failed to toggle flashlight", e)
            }
        }
    }
    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
            }
        }
    }
}
