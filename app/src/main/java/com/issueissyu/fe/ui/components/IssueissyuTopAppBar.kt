package com.issueissyu.fe.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueissyuTopAppBar(
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    titleText: String? = null,
    navigationContent: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            titleText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        navigationIcon = {
            when {
                navigationContent != null -> navigationContent()
                onBackClick != null -> {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            }
        },
        actions = actions,
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewIssueissyuTopAppBarNoTitle() {
    IssueissyuTheme {
        IssueissyuTopAppBar(
            onBackClick = {},
            titleText = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIssueissyuTopAppBarWithTitle() {
    IssueissyuTheme {
        IssueissyuTopAppBar(
            onBackClick = {},
            titleText = "이슈 작성"
        )
    }
}