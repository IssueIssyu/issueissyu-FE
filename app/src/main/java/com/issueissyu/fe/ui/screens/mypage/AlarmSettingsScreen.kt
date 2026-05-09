package com.issueissyu.fe.ui.screens.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    // 각 토글 상태
    //추후 datastore, viewModel 연동 필요
    var pinLike by remember { mutableStateOf(true) }
    var event by remember { mutableStateOf(true) }
    var popularPost by remember { mutableStateOf(true) }
    var storePromo by remember { mutableStateOf(true) }

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
                AlarmToggleItem(title = "내 핀 좋아요", checked = pinLike, onCheckedChange = { pinLike = it })
                AlarmToggleItem(title = "이벤트", checked = event, onCheckedChange = { event = it })
                AlarmToggleItem(title = "인기 게시글", checked = popularPost, onCheckedChange = { popularPost = it })
                AlarmToggleItem(title = "가게 홍보", checked = storePromo, onCheckedChange = { storePromo = it })
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
