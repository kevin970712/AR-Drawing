package com.android.ardrawing

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.cos
import kotlin.math.sin

// 圖片的狀態資料類別
data class ImageState(
    val opacity: Float = 0.5f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val isFlippedHorizontal: Boolean = false,
    val isFlippedVertical: Boolean = false,
    val isLocked: Boolean = false
)

class DrawingViewModel : ViewModel() {

    // 保存圖片的所有狀態
    private val _imageState = MutableStateFlow(ImageState())
    val imageState: StateFlow<ImageState> = _imageState.asStateFlow()

    // 功能開關
    private val _isCameraOn = MutableStateFlow(true)
    val isCameraOn = _isCameraOn.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn = _isFlashlightOn.asStateFlow()

    // UI 控制面板狀態
    private val _activePanel = MutableStateFlow(ControlPanel.NONE)
    val activePanel = _activePanel.asStateFlow()

    private val _isKeepScreenOn = MutableStateFlow(false)
    val isKeepScreenOn = _isKeepScreenOn.asStateFlow()

    enum class ControlPanel {
        OPACITY, SCALE, FLIP, NONE
    }

    // --- Actions ---

    fun toggleCamera() {
        _isCameraOn.value = !_isCameraOn.value
        if (!_isCameraOn.value) {
            _isFlashlightOn.value = false
            _activePanel.value = ControlPanel.NONE
        }
    }

    fun toggleFlashlight() {
        if (_isCameraOn.value) {
            _isFlashlightOn.value = !_isFlashlightOn.value
        }
    }

    // 閃光燈關閉
    fun setFlashlightOff() {
        _isFlashlightOn.value = false
    }

    fun toggleLock() {
        if (!_isCameraOn.value) return
        val current = _imageState.value
        _imageState.value = current.copy(isLocked = !current.isLocked)

        if (_imageState.value.isLocked) {
            _activePanel.value = ControlPanel.NONE
        }
    }

    fun togglePanel(panel: ControlPanel) {
        if (!_isCameraOn.value) return
        if (_activePanel.value == panel) {
            _activePanel.value = ControlPanel.NONE
        } else {
            _activePanel.value = panel
        }
    }

    fun closePanel() {
        _activePanel.value = ControlPanel.NONE
    }

    fun toggleFlipHorizontal() {
        if (!_isCameraOn.value) return
        val current = _imageState.value
        _imageState.value = current.copy(isFlippedHorizontal = !current.isFlippedHorizontal)
    }

    fun toggleFlipVertical() {
        if (!_isCameraOn.value) return
        val current = _imageState.value
        _imageState.value = current.copy(isFlippedVertical = !current.isFlippedVertical)
    }

    fun toggleKeepScreenOn() {
        if (!_isCameraOn.value) return
        _isKeepScreenOn.value = !_isKeepScreenOn.value
    }

    fun resetImage() {
        if (!_isCameraOn.value) return
        _imageState.value = ImageState()
    }

    // --- 更新數值  ---

    fun updateOpacity(value: Float) {
        _imageState.value = _imageState.value.copy(opacity = value)
    }

    fun updateScale(value: Float) {
        _imageState.value = _imageState.value.copy(scale = value.coerceIn(0.1f, 10f))
    }

    // 手勢更新邏輯
    fun onTransformGesture(panX: Float, panY: Float, zoomChange: Float, rotationChange: Float) {
        if (_imageState.value.isLocked) return

        val current = _imageState.value
        val newScale = (current.scale * zoomChange).coerceIn(0.1f, 10f)
        val newRotation = current.rotation + rotationChange

        val rad = Math.toRadians(current.rotation.toDouble())
        cos(rad)
        sin(rad)

        val newOffsetX = current.offsetX + (panX * current.scale)
        val newOffsetY = current.offsetY + (panY * current.scale)

        _imageState.value = current.copy(
            scale = newScale,
            rotation = newRotation,
            offsetX = newOffsetX,
            offsetY = newOffsetY
        )
    }
}