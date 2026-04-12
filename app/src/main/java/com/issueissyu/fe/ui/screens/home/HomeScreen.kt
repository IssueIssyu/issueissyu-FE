package com.issueissyu.fe.ui.screens.home

// 이 파일은 초기 세팅 확인을 위한 예시 화면입니다. 실제 개발 시 삭제되거나 대체될 예정입니다.

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.issueissyu.fe.ui.theme.LocalCustomAppColors
import com.issueissyu.fe.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState = viewModel.uiState.value
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = uiState.message,
            style = MaterialTheme.typography.headlineSmall,
            color = LocalCustomAppColors.current.brandColor
        )
    }
}
