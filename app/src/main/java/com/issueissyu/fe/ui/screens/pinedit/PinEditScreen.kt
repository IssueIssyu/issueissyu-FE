package com.issueissyu.fe.ui.screens.pinedit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.ui.components.CommonButton
import com.issueissyu.fe.ui.components.CommonTextField
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme

@Composable
fun PinEditScreen(
    pinId: String,
    onBackClick: () -> Unit,
    onEdited: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PinEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(pinId) {
        viewModel.loadPin(pinId)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is PinEditEvent.Edited -> onEdited(event.pinId)
            }
        }
    }

    PinEditContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onSubmit = viewModel::submitEdit,
        modifier = modifier
    )
}

@Composable
private fun PinEditContent(
    uiState: PinEditUiState,
    onBackClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IssueissyuTopAppBar(
            onBackClick = onBackClick,
            titleText = "핀 수정"
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CommonTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = "제목",
                placeholder = "제목을 입력하세요.",
                maxLength = 50
            )

            CommonTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = "상세 설명",
                placeholder = "상세 설명을 작성해 주세요.",
                maxLines = 8,
                maxLength = 500,
                textStyle = IssueTypo.Regular16
            )

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = IssueTypo.Regular12.copy(color = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        HorizontalDivider(color = Gray_3, thickness = 1.dp)

        CommonButton(
            onClick = onSubmit,
            text = if (uiState.isSubmitting) "수정 중..." else "수정 완료",
            isEnabled = !uiState.isLoading && !uiState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 16.dp)
        )
    }
}

@Preview(name = "PinEdit", showBackground = true, heightDp = 900)
@Composable
private fun PinEditContentPreview() {
    IssueissyuTheme {
        PinEditContent(
            uiState = PinEditUiState(
                pinId = "preview",
                title = "공원 벤치 파손",
                description = "어린이 공원 내 벤치가 파손되어 위험합니다."
            ),
            onBackClick = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onSubmit = {}
        )
    }
}
