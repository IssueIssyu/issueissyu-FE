package com.issueissyu.fe.ui.screens.mypage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White
import com.issueissyu.fe.ui.theme.suiteFontFamily

@Composable
fun AlarmSettingScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // 각 토글 상태
    var pinLike by remember { mutableStateOf(true) }
    var event by remember { mutableStateOf(true) }
    var popularPost by remember { mutableStateOf(true) }
    var storePromo by remember { mutableStateOf(true) }

    //알람 권한 확인
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
            openAppSettings()  // 권한 없으면 설정으로
        } else {
            update(!current)
        }
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

        //content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(31.dp, 20.dp)
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
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                AlarmToggleItem("내 핀 좋아요", pinLike) { onToggle(pinLike)  { pinLike = it }}
                AlarmToggleItem("이벤트", event) { onToggle(event)  { event = it }}
                AlarmToggleItem("인기 게시글", popularPost) { onToggle(popularPost)  { popularPost = it }}
                AlarmToggleItem("가게 홍보", storePromo) { onToggle(storePromo)  { storePromo = it }}
            }
        }
    }
}

@Composable
fun AlarmToggleItem(
    title: String,
    checked: Boolean,
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
        onBackClick = {}
    )
}
