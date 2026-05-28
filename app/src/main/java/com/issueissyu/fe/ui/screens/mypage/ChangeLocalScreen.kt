package com.issueissyu.fe.ui.screens.mypage

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.ui.components.location.LocalAreaPickerContent

@Composable
fun ChangeLocalScreen(
    onBackClick: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChangeLocalViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                ChangeLocalViewModel.UiEvent.Completed -> onComplete()
                is ChangeLocalViewModel.UiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LocalAreaPickerContent(
        modifier = modifier,
        titleText = "동네 변경",
        onBackClick = onBackClick,
        addressText = uiState.addressText,
        isLoading = uiState.isLoading,
        isConfirmEnabled = uiState.isLocationReady,
        onConfirmClick = viewModel::updateLocation,
        onCurrentLocationReady = viewModel::onCurrentLocationReady,
    )
}
