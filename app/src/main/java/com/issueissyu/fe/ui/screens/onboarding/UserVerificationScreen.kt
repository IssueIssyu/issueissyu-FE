package com.issueissyu.fe.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.components.CommonButton
import com.issueissyu.fe.ui.components.CommonTextField
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.*
import com.issueissyu.fe.ui.viewmodels.UserVerificationViewModel
import com.issueissyu.fe.ui.viewmodels.UserVerificationViewModel.Companion.PHONE_NUMBER_LENGTH

@Composable
fun UserVerificationScreen(
    viewModel: UserVerificationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onVerificationComplete: (nickname: String, email: String, phoneNumber: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val emailDomains = listOf("선택", "naver.com", "gmail.com", "daum.net", "직접 입력")

    // 인증번호 자동 확인
    LaunchedEffect(uiState.verificationCode) {
        if (uiState.verificationCode.length == 6 && uiState.isVerificationCodeSent) {
            viewModel.verifyCode()
        }
    }

    UserVerificationContent(
        nickname = uiState.nickname,
        isNicknameChecked = uiState.isNicknameChecked,
        nicknameError = uiState.nicknameError,
        isCheckingNickname = uiState.isCheckingNickName,
        emailId = uiState.emailId,
        emailDomain = uiState.emailDomain,
        customDomain = uiState.customDomain,
        showDomainDropdown = uiState.showDomainDropdown,
        phoneNumber = uiState.phoneNumber,
        phoneError = uiState.phoneError,
        isVerificationCodeSent = uiState.isVerificationCodeSent,
        isSendingCode = uiState.isSendingCode,
        verificationCode = uiState.verificationCode,
        isCodeVerified = uiState.isCodeVerified,
        codeError = uiState.codeError,
        isVerifyingCode = uiState.isVerifyingCode,
        isSignupEnabled = uiState.isSignupEnabled,
        emailDomains = emailDomains,
        onNicknameChange = viewModel::onNicknameChange,
        onCheckNicknameDuplicate = viewModel::checkNicknameDuplicate,
        onEmailIdChange = viewModel::onEmailIdChange,
        onEmailDomainChange = viewModel::onEmailDomainChange,
        onCustomDomainChange = viewModel::onCustomDomainChange,
        onToggleDropdown = viewModel::toggleDomainDropdown,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onSendVerificationCode = viewModel::sendVerificationCode,
        onVerificationCodeChange = viewModel::onVerificationCodeChange,
        onSignupClick = {
            onVerificationComplete(
                uiState.nickname,
                viewModel.getFullEmail(),
                uiState.phoneNumber
            )
        },
        onBackClick = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserVerificationContent(
    nickname: String,
    isNicknameChecked: Boolean,
    nicknameError: String?,
    isCheckingNickname: Boolean,
    emailId: String,
    emailDomain: String,
    customDomain: String,
    showDomainDropdown: Boolean,
    phoneNumber: String,
    phoneError: String?,
    isVerificationCodeSent: Boolean,
    isSendingCode: Boolean,
    verificationCode: String,
    isCodeVerified: Boolean,
    codeError: String?,
    isVerifyingCode: Boolean,
    isSignupEnabled: Boolean,
    emailDomains: List<String>,
    onNicknameChange: (String) -> Unit,
    onCheckNicknameDuplicate: () -> Unit,
    onEmailIdChange: (String) -> Unit,
    onEmailDomainChange: (String) -> Unit,
    onCustomDomainChange: (String) -> Unit,
    onToggleDropdown: () -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onSendVerificationCode: () -> Unit,
    onVerificationCodeChange: (String) -> Unit,
    onSignupClick: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = White,
        topBar = {
            IssueissyuTopAppBar(
                onBackClick = onBackClick,
                titleText = "본인인증"
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 31.dp)
                    .padding(bottom = 24.dp)
            ) {
                // 가입하기 버튼
                CommonButton(
                    text = "가입하기",
                    onClick = onSignupClick,
                    isEnabled = isSignupEnabled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 31.dp)
                .padding(top = 32.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 프로필 이미지
            ProfileImageSection()

            Spacer(modifier = Modifier.height(17.dp))

            // 닉네임 입력
            NicknameSection(
                nickname = nickname,
                isNicknameChecked = isNicknameChecked,
                nicknameError = nicknameError,
                isCheckingNickname = isCheckingNickname,
                onNicknameChange = onNicknameChange,
                onCheckDuplicate = onCheckNicknameDuplicate
            )

            // 이메일 입력
            EmailSection(
                emailId = emailId,
                emailDomain = emailDomain,
                customDomain = customDomain,
                showDomainDropdown = showDomainDropdown,
                emailDomains = emailDomains,
                onEmailIdChange = onEmailIdChange,
                onEmailDomainChange = onEmailDomainChange,
                onCustomDomainChange = onCustomDomainChange,
                onToggleDropdown = onToggleDropdown
            )

            // 휴대폰 번호
            PhoneNumberSection(
                phoneNumber = phoneNumber,
                phoneError = phoneError,
                isVerificationCodeSent = isVerificationCodeSent,
                isSendingCode = isSendingCode,
                onPhoneNumberChange = onPhoneNumberChange,
                onSendVerificationCode = onSendVerificationCode
            )

            // 인증번호 입력
            VerificationCodeSection(
                verificationCode = verificationCode,
                isCodeVerified = isCodeVerified,
                codeError = codeError,
                onVerificationCodeChange = onVerificationCodeChange
            )
        }
    }
}

@Composable
private fun ProfileImageSection() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Gray_3),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.img_character_face),
            contentDescription = "프로필 이미지",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun NicknameSection(
    nickname: String,
    isNicknameChecked: Boolean,
    nicknameError: String?,
    isCheckingNickname: Boolean,
    onNicknameChange: (String) -> Unit,
    onCheckDuplicate: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 닉네임 입력 필드
        CommonTextField(
            label = "닉네임",
            placeholder = "닉네임을 입력하세요",
            value = nickname,
            onValueChange = onNicknameChange,
            maxLength = 10
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (nicknameError != null) {
                    Text(
                        text = nicknameError,
                        style = IssueTypo.Regular15.copy(color = Issue)
                    )
                } else if (!isNicknameChecked && nickname.isNotEmpty()) {
                    Text(
                        text = "닉네임 중복을 확인해주세요",
                        style = IssueTypo.Regular15.copy(color = Issue)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 중복 확인 버튼
            Button(
                onClick = onCheckDuplicate,
                enabled = nickname.isNotEmpty() && nicknameError == null && !isCheckingNickname,
                modifier = Modifier.size(70.dp, 30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColor,
                    disabledContainerColor = Gray_5
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(5.dp)
            ) {
                Text(
                    text = if (isNicknameChecked) "확인됨" else "중복 확인",
                    style = IssueTypo.Bold12.copy(color = White)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailSection(
    emailId: String,
    emailDomain: String,
    customDomain: String,
    showDomainDropdown: Boolean,
    emailDomains: List<String>,
    onEmailIdChange: (String) -> Unit,
    onEmailDomainChange: (String) -> Unit,
    onCustomDomainChange: (String) -> Unit,
    onToggleDropdown: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "이메일",
            style = IssueTypo.Regular18.copy(color = Title),
            modifier = Modifier.padding(start = 5.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 이메일 아이디 입력
            CommonTextField(
                value = emailId,
                onValueChange = onEmailIdChange,
                placeholder = "이메일",
                modifier = Modifier.weight(1f)
            )

            Text("@", style = IssueTypo.Bold18.copy(color = Gray_5))

            // 도메인 선택/입력
            Box(modifier = Modifier.weight(1f)) {
                if (emailDomain == "직접 입력") {
                    CommonTextField(
                        value = customDomain,
                        onValueChange = onCustomDomainChange,
                        placeholder = "직접 입력",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = showDomainDropdown,
                        onExpandedChange = { onToggleDropdown() }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(White, RoundedCornerShape(15.dp))
                                .border(1.dp, Gray_4, RoundedCornerShape(15.dp))
                                .clip(RoundedCornerShape(15.dp))
                                .menuAnchor(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 15.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = emailDomain,
                                    style = IssueTypo.Bold18.copy(
                                        color = if (emailDomain == "선택") Gray_4 else Title
                                    )
                                )
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDomainDropdown)
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = showDomainDropdown,
                            onDismissRequest = onToggleDropdown
                        ) {
                            emailDomains.forEach { domain ->
                                DropdownMenuItem(
                                    text = { Text(domain, style = IssueTypo.Regular18) },
                                    onClick = { onEmailDomainChange(domain) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneNumberSection(
    phoneNumber: String,
    phoneError: String?,
    isVerificationCodeSent: Boolean,
    isSendingCode: Boolean,
    onPhoneNumberChange: (String) -> Unit,
    onSendVerificationCode: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 휴대폰 번호 입력
        CommonTextField(
            label = "휴대폰 번호",
            placeholder = "'-' 없이 번호만 입력해주세요",
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            maxLength = PHONE_NUMBER_LENGTH
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (phoneError != null) {
                    Text(
                        phoneError,
                        style = IssueTypo.Regular12.copy(color = Issue)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 인증번호 받기/재발송 버튼
            Button(
                onClick = onSendVerificationCode,
                enabled = phoneNumber.length >= 10 && phoneError == null && !isSendingCode,
                modifier = Modifier.size(70.dp, 30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColor,
                    disabledContainerColor = Gray_5
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(5.dp)
            ) {
                Text(
                    text = if (isSendingCode) "전송중" else {
                        if (isVerificationCodeSent) "재발송" else "인증번호"
                    },
                    style = IssueTypo.Bold12.copy(color = White)
                )
            }
        }
    }
}

@Composable
private fun VerificationCodeSection(
    verificationCode: String,
    isCodeVerified: Boolean,
    codeError: String?,
    onVerificationCodeChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 인증번호 입력 필드
        CommonTextField(
            label = "인증번호",
            placeholder = "인증번호를 입력해주세요",
            value = verificationCode,
            onValueChange = onVerificationCodeChange,
            maxLength = 6
        )

        // 에러 또는 확인 완료 메시지
        if (codeError != null) {
            Text(
                text = codeError,
                style = IssueTypo.Regular12.copy(color = Issue),
                modifier = Modifier.padding(horizontal = 5.dp)
            )
        } else if (isCodeVerified) {
            Text(
                text = "인증이 완료되었습니다",
                style = IssueTypo.Regular12.copy(color = BrandColor),
                modifier = Modifier.padding(horizontal = 5.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UserVerificationScreenPreview() {
    UserVerificationContent(
        nickname = "테스트",
        isNicknameChecked = false,
        nicknameError = null,
        isCheckingNickname = false,
        emailId = "test",
        emailDomain = "선택",
        customDomain = "",
        showDomainDropdown = false,
        phoneNumber = "01012345678",
        phoneError = null,
        isVerificationCodeSent = true,
        isSendingCode = false,
        verificationCode = "123456",
        isCodeVerified = false,
        codeError = null,
        isVerifyingCode = false,
        isSignupEnabled = false,
        emailDomains = listOf("선택", "naver.com", "gmail.com", "daum.net", "직접 입력"),
        onNicknameChange = {},
        onCheckNicknameDuplicate = {},
        onEmailIdChange = {},
        onEmailDomainChange = {},
        onCustomDomainChange = {},
        onToggleDropdown = {},
        onPhoneNumberChange = {},
        onSendVerificationCode = {},
        onVerificationCodeChange = {},
        onSignupClick = {}
    )
}