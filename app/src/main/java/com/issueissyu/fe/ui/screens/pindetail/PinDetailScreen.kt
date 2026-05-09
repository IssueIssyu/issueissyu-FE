package com.issueissyu.fe.ui.screens.pindetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.data.model.Pin
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar

@Composable
fun PinDetailScreen(
    pinId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PinDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(pinId) {
        viewModel.loadPin(pinId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IssueissyuTopAppBar(
            onBackClick = onBackClick,
            titleText = "핀 상세"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    Text(text = "핀 정보를 불러오는 중...")
                }

                uiState.errorMessage != null -> {
                    Text(text = uiState.errorMessage)
                }

                uiState.pin != null -> {
                    PinDetailPlaceholderContent(
                        pinId = pinId,
                        pin = uiState.pin
                    )
                }

                else -> {
                    Text(text = "핀 상세 화면은 추후 구현 예정입니다.")
                }
            }
        }
    }
}

@Composable
private fun PinDetailPlaceholderContent(
    pinId: String,
    pin: Pin
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "pinId: $pinId")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "title: ${pin.title}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "address: ${pin.address}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "category: ${pin.category}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "detail: ${pin.detail::class.simpleName}")
    }
}
