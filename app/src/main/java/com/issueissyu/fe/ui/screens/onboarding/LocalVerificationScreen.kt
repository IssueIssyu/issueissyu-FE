package com.issueissyu.fe.ui.screens.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.ui.components.location.LocalAreaPickerContent
import com.issueissyu.fe.ui.theme.IssueissyuTheme

@Composable
fun LocalVerificationScreen(
    onCompleteRegisterClick: () -> Unit = {},
    viewModel: LocalVerificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is LocalVerificationViewModel.UiEvent.NavigateNext -> onCompleteRegisterClick()
                is LocalVerificationViewModel.UiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LocalAreaPickerContent(
            titleText = "동네 설정",
            onBackClick = null,
            addressText = uiState.addressText,
            isLoading = uiState.isLoading,
            isConfirmEnabled = uiState.isLocationReady,
            onConfirmClick = viewModel::registerLocation,
            onCurrentLocationReady = viewModel::onCurrentLocationReady,
            onLocationUnavailable = viewModel::onLocationUnavailable,
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLocalVerificationScreen() {
    IssueissyuTheme {
        LocalVerificationScreen()
    }
}
