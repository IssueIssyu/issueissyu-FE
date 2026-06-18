package com.issueissyu.fe.ui.screens.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.SavedStateHandle
import coil.compose.AsyncImage
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.components.Dialog
import com.issueissyu.fe.ui.components.InfoDialog
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.components.ProfileImageFrame
import com.issueissyu.fe.ui.theme.CommunicationContainerLight
import com.issueissyu.fe.ui.theme.Gray_1
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Text
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White
import com.issueissyu.fe.ui.theme.suiteFontFamily

const val MYPAGE_REFRESH_KEY = "mypage_refresh"

sealed class MyPageEvent {
    data object NavigateBack: MyPageEvent()
    data object NavigateToProfile: MyPageEvent()
    data object NavigateToLocal: MyPageEvent()
    data object NavigateToIssue: MyPageEvent()
    data object NavigateToSolverParticipation: MyPageEvent()
    data object NavigateToSettingAlarm: MyPageEvent()
    data object NavigateToLanding: MyPageEvent()
    data object NavigateToLogin: MyPageEvent()
    data object NavigateToTerm: MyPageEvent()
}

@Composable
fun MyPageScreen(
    onEvent: (MyPageEvent) -> Unit,
    modifier: Modifier,
    savedStateHandle: SavedStateHandle? = null,
    viewModel: MyPageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authActionState by viewModel.authActionState.collectAsStateWithLifecycle()

    val shouldRefresh by savedStateHandle
        ?.getStateFlow(MYPAGE_REFRESH_KEY, false)
        ?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(false) }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.loadCollections(force = true)
            savedStateHandle?.set(MYPAGE_REFRESH_KEY, false)
        }
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var actionErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authActionState) {
        when (val state = authActionState) {
            MyPageViewModel.AuthActionState.Success -> {
                showLogoutDialog = false
                showWithdrawDialog = false
                // 세션 해제 시 AppNavGraph가 로그인 화면으로 이동함
                viewModel.resetAuthActionState()
            }
            is MyPageViewModel.AuthActionState.Error -> {
                showLogoutDialog = false
                showWithdrawDialog = false
                actionErrorMessage = state.message
                viewModel.resetAuthActionState()
            }
            else -> Unit
        }
    }

    val isAuthActionLoading = authActionState is MyPageViewModel.AuthActionState.Loading

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null && uiState.nickname.isBlank() -> {
                MyPageErrorState(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = viewModel::loadCollections,
                )
            }

            else -> {
                MyPageContent(
                    uiState = uiState,
                    onEvent = onEvent,
                    onLogoutClick = { showLogoutDialog = true },
                    onWithdrawClick = { showWithdrawDialog = true },
                )
            }
        }

        if (showLogoutDialog) {
            Dialog(
                title = "로그아웃",
                message = "정말 로그아웃 하시겠어요?\n언제든지 다시 돌아올 수 있어요!",
                confirmText = "로그아웃",
                onDismiss = {
                    if (!isAuthActionLoading) {
                        showLogoutDialog = false
                    }
                },
                onConfirm = {
                    viewModel.logout()
                },
            )
        }

        if (showWithdrawDialog) {
            Dialog(
                title = "회원탈퇴",
                message = "정말 탈퇴하시겠어요?\n그동안 모은 핀과 활동 기록이\n모두 삭제돼요",
                confirmText = "탈퇴하기",
                isWarning = true,
                onDismiss = {
                    if (!isAuthActionLoading) {
                        showWithdrawDialog = false
                    }
                },
                onConfirm = {
                    viewModel.withdraw()
                },
            )
        }

        actionErrorMessage?.let { message ->
            InfoDialog(
                title = "안내",
                message = message,
                onConfirm = { actionErrorMessage = null },
            )
        }

        if (isAuthActionLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun MyPageContent(
    uiState: MyPageUiState,
    onEvent: (MyPageEvent) -> Unit,
    onLogoutClick: () -> Unit,
    onWithdrawClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        IssueissyuTopAppBar(
            titleText = "마이페이지",
            onBackClick = { onEvent(MyPageEvent.NavigateBack) },
            modifier = Modifier.statusBarsPadding(),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            ProfileImageFrame(
                size = 100.dp,
                imageUrl = uiState.profileImageUrl,
                borderWidth = 0.dp,
            )

            Spacer(modifier = Modifier.size(30.dp))

            Row(
                modifier = Modifier.clickable(
                    onClick = { onEvent(MyPageEvent.NavigateToProfile) },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = uiState.nickname,
                    fontFamily = suiteFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Title,
                )

                Spacer(modifier = Modifier.size(10.dp))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "프로필 수정",
                    tint = Title,
                    modifier = Modifier.size(25.dp),
                )
            }
        }

        MyPinsSection(pins = uiState.bookmarkedPins)

        Column(modifier = Modifier.fillMaxWidth()) {
            NavBar(
                icon = Icons.Outlined.LocationOn,
                title = "동네 변경",
                onNavClick = { onEvent(MyPageEvent.NavigateToLocal) },
            )
            NavBar(
                icon = Icons.Default.LocationOn,
                title = "내 이슈",
                onNavClick = { onEvent(MyPageEvent.NavigateToIssue) },
            )
            NavBar(
                icon = Icons.Outlined.VolunteerActivism,
                title = "해결 참여",
                onNavClick = { onEvent(MyPageEvent.NavigateToSolverParticipation) },
            )
            NavBar(
                icon = Icons.Outlined.Notifications,
                title = "알림 설정",
                onNavClick = { onEvent(MyPageEvent.NavigateToSettingAlarm) },
            )
            NavBar(
                icon = Icons.Outlined.MenuBook,
                title = "도움말",
                onNavClick = { onEvent(MyPageEvent.NavigateToLanding) },
            )
            NavBar(
                icon = Icons.Outlined.Assignment,
                title = "이용 약관",
                onNavClick = { onEvent(MyPageEvent.NavigateToTerm) },
            )
            NavBar(
                icon = Icons.Outlined.Logout,
                title = "로그아웃",
                onNavClick = onLogoutClick,
            )

            Text(
                text = "회원탈퇴",
                style = IssueTypo.Regular16.copy(color = Issue),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .clickable(onClick = onWithdrawClick),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MyPageErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = IssueTypo.Regular16.copy(color = Text),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text(text = "다시 시도")
        }
    }
}

@Composable
private fun MyPinsSection(pins: List<Pin>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "내 핀들",
            style = IssueTypo.Bold18.copy(color = Title)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (pins.isEmpty()) {
            // 핀이 없을 때
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Gray_1, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "아직 수집한 핀이 없어요",
                    style = IssueTypo.Regular16.copy(color = Gray_5)
                )
            }
        } else {
            // 핀 리스트 (가로 스크롤)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(pins.size) { index ->
                    PinCard(pin = pins[index])
                }
            }
        }
    }
}

@Composable
private fun PinCard(pin: Pin) {
    Column(
        modifier = Modifier
            .size(100.dp, 170.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(15.dp),
                clip = false
            )
            .background(CommunicationContainerLight, RoundedCornerShape(15.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // 핀 이미지
        AsyncImage(
            model = pin.imageUrl,
            contentDescription = pin.name,
            modifier = Modifier.size(100.dp),
            contentScale = ContentScale.Fit,
            placeholder = painterResource(R.drawable.ic_character_default),
            error = painterResource(R.drawable.ic_character_default),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 핀 이름
        Text(
            text = pin.name,
            style = IssueTypo.Bold18.copy(
                fontSize = 16.sp,
                color = Text
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NavBar(
    icon: ImageVector,
    title: String,
    onNavClick: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavClick)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Gray_5,
                    modifier = Modifier.size(20.dp),
                )

                Spacer(modifier = Modifier.size(10.dp))

                Text(
                    text = title,
                    fontFamily = suiteFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Text,
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Gray_5,
                modifier = Modifier.size(20.dp),
            )
        }
    }

    HorizontalDivider(
        thickness = 1.dp,
        color = Gray_5,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewMyPageScreen(){
    MyPageScreen(
        onEvent = {},
        modifier = Modifier
    )
}
