package com.issueissyu.fe.ui.screens.onboarding

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.ui.components.location.LocalAreaPickerContent
import com.issueissyu.fe.ui.theme.IssueissyuTheme

@Composable
fun LocalVerificationScreen(
    onCompleteRegisterClick: () -> Unit = {},
    onSwitchAccountClick: (() -> Unit)? = null,
    viewModel: LocalVerificationViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is LocalVerificationViewModel.UiEvent.NavigateNext -> onCompleteRegisterClick()
                is LocalVerificationViewModel.UiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LocalAreaPickerContent(
        titleText = "동네 설정",
        onBackClick = null,
        navigationContent = { OnboardingSwitchAccountNavigationContent(onSwitchAccountClick)
        },
        addressText = uiState.addressText,
        isLoading = uiState.isLoading,
        isConfirmEnabled = uiState.isLocationReady,
        onConfirmClick = viewModel::registerLocation,
        onCurrentLocationReady = viewModel::onCurrentLocationReady,
        onLocationUnavailable = viewModel::onLocationUnavailable,
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewLocalVerificationScreen() {
    IssueissyuTheme {
        LocalVerificationScreen()
    }
}