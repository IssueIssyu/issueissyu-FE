package com.issueissyu.fe.ui.screens.mypage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White
import com.issueissyu.fe.ui.theme.suiteFontFamily

@Composable
fun AlarmSettingScreen(
    onBackClick: () -> Unit,
    viewModel: AlarmSettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    // 권한이 없어 설정 앱으로 보낸 토글 동작을 보관했다가 복귀 후 권한이 허용되면 이어서 처리
    var pendingEnable by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is AlarmSettingsViewModel.UiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    //권한 x -> 설정 앱으로 이동
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    // 토글 켤 때 권한 체크
    fun onToggle(current: Boolean, update: (Boolean) -> Unit) {
        if (!current && !hasNotificationPermission()) {
            pendingEnable = { update(true) }  // 복귀 후 권한 허용되면 이어서 켜기
            openAppSettings()  // 권한 없으면 설정으로
        } else {
            update(!current)
        }
    }

    // 설정 앱에서 권한을 허용하고 돌아온 경우, 보류해둔 토글을 자동으로 처리
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val action = pendingEnable
                pendingEnable = null
                if (action != null && hasNotificationPermission()) {
                    action()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
        .fillMaxSize()
        .background(White)
    ) {
        // 상단 바
        IssueissyuTopAppBar(
            titleText = "알림 설정",
            onBackClick = onBackClick
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 31.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            fontFamily = suiteFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Gray_6,
                            textAlign = TextAlign.Center,
                        )
                        Button(onClick = viewModel::loadAlarmSettings) {
                            Text("다시 시도")
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(31.dp, 20.dp),
                ){
                    Text(
                        text = "여기서 끄면 비슷한 내용의 알림은 보내지 않아요",
                        fontFamily = suiteFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Gray_6,
                    )

                    Spacer(modifier = Modifier.height(50.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                    ) {
                        AlarmToggleItem(
                            title = "내 핀 좋아요",
                            checked = uiState.pinLike,
                            enabled = !uiState.isUpdating,
                        ) {
                            onToggle(uiState.pinLike) { viewModel.updatePinLike(it) }
                        }
                        AlarmToggleItem(
                            title = "이벤트",
                            checked = uiState.event,
                            enabled = !uiState.isUpdating,
                        ) {
                            onToggle(uiState.event) { viewModel.updateEvent(it) }
                        }
                        AlarmToggleItem(
                            title = "인기 게시글",
                            checked = uiState.popularPost,
                            enabled = !uiState.isUpdating,
                        ) {
                            onToggle(uiState.popularPost) { viewModel.updatePopularPost(it) }
                        }
                        AlarmToggleItem(
                            title = "가게 홍보",
                            checked = uiState.storePromo,
                            enabled = !uiState.isUpdating,
                        ) {
                            onToggle(uiState.storePromo) { viewModel.updateStorePromo(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmToggleItem(
    title: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = IssueTypo.Bold18.copy(color = Title, fontSize = 20.sp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = BrandColor
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmSettingScreenPreview() {
    AlarmSettingScreen(
        onBackClick = {},
    )
}
