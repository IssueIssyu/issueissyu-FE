package com.issueissyu.fe.ui.screens.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Text
import com.issueissyu.fe.ui.theme.White

enum class TermsType {
    SERVICE, PRIVACY, LOCATION
}

@Composable
fun MyPageTermScreen(
    onBackClick: () -> Unit,
    onTermsDetailClick: (TermsType) -> Unit = {},
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ){
        // 상단 바
        IssueissyuTopAppBar(
            titleText = "이용 약관",
            onBackClick = onBackClick
        )

        //이용 약관 선택
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ){
            selectTerm("서비스 이용약관 동의", { onTermsDetailClick(TermsType.SERVICE) })
            selectTerm("개인정보 수집 및 이용 동의", { onTermsDetailClick(TermsType.PRIVACY) })
            selectTerm("위치기반 서비스 이용약관 동의", { onTermsDetailClick(TermsType.LOCATION) })
        }
    }
}

@Composable
fun selectTerm(
    text: String,
    onNavClick: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavClick)
            .padding(20.dp, 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = IssueTypo.Bold18.copy(color = Text),
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Gray_5,
            modifier = Modifier.size(35.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMyPageTermScreen(){
    MyPageTermScreen(
        onBackClick = {}
    )
}