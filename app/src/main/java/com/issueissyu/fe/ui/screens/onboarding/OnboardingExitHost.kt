package com.issueissyu.fe.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Text
import com.issueissyu.fe.ui.theme.White

@Composable
fun OnboardingExitHost(
    onNavigateToLogin: () -> Unit,
    viewModel: OnboardingExitViewModel = hiltViewModel(),
    content: @Composable (onSwitchAccountClick: () -> Unit) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onNavigateToLoginUpdated by rememberUpdatedState(onNavigateToLogin)

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                OnboardingExitViewModel.Effect.NavigateToLogin -> {
                    onNavigateToLoginUpdated()
                    viewModel.onNavigateToLoginDispatched()
                }
            }
        }
    }

    if (uiState.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmDialog() },
            title = {
                Text(
                    text = "다른 계정으로 로그인",
                    style = IssueTypo.Bold18,
                )
            },
            text = {
                Text(
                    text = "온보딩을 중단하고 로그아웃할까요?\n입력한 내용은 저장되지 않습니다.",
                    style = IssueTypo.Regular15.copy(color = Text),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmSwitchAccount() },
                    enabled = !uiState.isExiting,
                ) {
                    Text("로그아웃", style = IssueTypo.Bold12.copy(color = BrandColor))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissConfirmDialog() },
                    enabled = !uiState.isExiting,
                ) {
                    Text("취소", style = IssueTypo.Bold12.copy(color = Gray_5))
                }
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content(viewModel::requestSwitchAccount)

        if (uiState.isExiting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = BrandColor)
            }
        }
    }
}

@Composable
fun OnboardingSwitchAccountNavigationContent(
    onSwitchAccountClick: (() -> Unit)?,
) {
    onSwitchAccountClick?.let { onClick ->
        OnboardingSwitchAccountAction(onClick = onClick)
    }
}

@Composable
fun OnboardingSwitchAccountAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "다른 계정으로 로그인",
                modifier = Modifier.size(18.dp),
                tint = Gray_5,
            )

            Spacer(modifier=Modifier.width(5.dp))

            Text(
                text = "다른 계정으로 로그인",
                style = IssueTypo.Regular12.copy(color = Gray_5),
            )
        }
    }
}
