package com.issueissyu.fe.core.media

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.issueissyu.fe.ui.components.PhotoAddSourceBottomSheet

@Stable
class PhotoSourcePickerState internal constructor(
    val showSourceSheet: () -> Unit,
    internal val photoSourceBottomSheet: @Composable () -> Unit,
) {
    @Composable
    fun PhotoSourceBottomSheet() {
        photoSourceBottomSheet()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberPhotoSourcePicker(
    currentCount: Int,
    maxCount: Int,
    onImagesPicked: (List<String>) -> Unit,
    cameraFileNamePrefix: String,
): PhotoSourcePickerState {
    val context = LocalContext.current
    var showPhotoSourceSheet by remember { mutableStateOf(false) }

    val remainingSlots = (maxCount - currentCount).coerceAtLeast(0)
    val albumPickRequest = remember {
        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
    }

    var launchAlbumPicker by remember { mutableStateOf<(() -> Unit)?>(null) }

    if (remainingSlots > 1) {
        key(remainingSlots) {
            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = remainingSlots),
            ) { uris ->
                onImagesPicked(uris.map { it.toString() })
            }
            SideEffect {
                launchAlbumPicker = {
                    imagePickerLauncher.launch(albumPickRequest)
                }
            }
        }
    } else if (remainingSlots == 1) {
        key("single-album") {
            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia(),
            ) { uri ->
                uri?.let { picked -> onImagesPicked(listOf(picked.toString())) }
            }
            SideEffect {
                launchAlbumPicker = {
                    imagePickerLauncher.launch(albumPickRequest)
                }
            }
        }
    } else {
        SideEffect {
            launchAlbumPicker = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        when {
            bitmap == null -> Unit
            else -> {
                val uri = CapturedImageSaver.saveJpegToCache(context, bitmap, cameraFileNamePrefix)
                if (uri == null) {
                    Toast.makeText(context, "사진을 저장하지 못했습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    onImagesPicked(listOf(uri))
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    val launchCamera = remember(cameraLauncher, cameraPermissionLauncher, context) {
        {
            showPhotoSourceSheet = false
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                cameraLauncher.launch(null)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    val launchAlbum: () -> Unit = remember {
        {
            showPhotoSourceSheet = false
            launchAlbumPicker?.invoke()
        }
    }

    return PhotoSourcePickerState(
        showSourceSheet = { showPhotoSourceSheet = true },
        photoSourceBottomSheet = {
            if (showPhotoSourceSheet) {
                PhotoAddSourceBottomSheet(
                    onDismissRequest = { showPhotoSourceSheet = false },
                    onAlbumClick = launchAlbum,
                    onCameraClick = launchCamera,
                )
            }
        },
    )
}
