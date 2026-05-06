package com.issueissyu.fe.ui.screens.onboarding

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.components.CommonButton
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.CommunicationContainerLight
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.White

@Composable
fun CompleteScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToLanding: () -> Unit
){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Scaffold(
            topBar = {
                IssueissyuTopAppBar()
            },
            bottomBar = {
                CommonButton(
                    onClick = onNavigateToMain,
                    text = "확인",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 31.dp)
                        .padding(bottom = 45.dp)
                )
            },
            containerColor = White
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(31.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ){
                Spacer(modifier = Modifier.height(25.dp))

                Image(
                    painter = painterResource(R.drawable.img_character_2),
                    modifier = Modifier
                        .size(300.dp),
                    contentDescription = "캐릭터",
                )

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = "환영합니다.\n지금부터 서비스를 자유롭게 \n이용해 보세요!",
                    style = IssueTypo.Bold18,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .background(CommunicationContainerLight, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "도움말은 마이페이지에서 다시 확인할 수 있어요",
                        style = IssueTypo.Bold12
                    )
                }

                CommonButton(
                    onClick = onNavigateToLanding,
                    text = "도움말 보기",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CompleteScreenPreview(){
    CompleteScreen(
        onNavigateToMain = {},
        onNavigateToLanding = {}
    )
}