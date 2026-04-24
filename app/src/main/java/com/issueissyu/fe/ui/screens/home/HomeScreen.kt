package com.issueissyu.fe.ui.screens.home

// 이 파일은 초기 세팅 확인을 위한 예시 화면입니다. 실제 개발 시 삭제되거나 대체될 예정입니다.

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.issueissyu.fe.ui.components.CategoryButtons
import com.issueissyu.fe.ui.components.CategoryItem
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.LightOrange
import com.issueissyu.fe.ui.theme.ShopContainerLight
import com.issueissyu.fe.ui.theme.IssueContainerLight
import com.issueissyu.fe.ui.theme.CommunicationContainerLight
import com.issueissyu.fe.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    onBackClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.value
    Scaffold(
        topBar = {
            IssueissyuTopAppBar(onBackClick = onBackClick)
        }
    ) { paddingValues ->
        var selectedCategory by remember { mutableStateOf<String?>(null) }
        val sampleCategories = remember {
            listOf(
                CategoryItem("이슈", R.drawable.issue, Issue, IssueContainerLight),
                CategoryItem("소통", R.drawable.communicate, Communication, LightOrange),
                CategoryItem("가게", R.drawable.shop, BrandColor, CommunicationContainerLight),
                CategoryItem("축제", R.drawable.festival, Festival, ShopContainerLight)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // 상단에 배치
        ) {
            CategoryButtons(
                categories = sampleCategories,
                selectedCategory = selectedCategory,
                onCategorySelected = { categoryName -> selectedCategory = categoryName },
                onNotificationClick = {}
            )
            Text(
                text = uiState.message,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen(onBackClick = {})
}