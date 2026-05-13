package com.issueissyu.fe.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.components.CommonButton
import com.issueissyu.fe.ui.components.CommonTextField
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.White
import com.issueissyu.fe.ui.viewmodels.SignUpUiState
import com.issueissyu.fe.ui.viewmodels.SignUpViewModel

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = hiltViewModel(),
    onNavigateToVerification: () -> Unit
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.signUpSuccess) {
        if(uiState.signUpSuccess){
            onNavigateToVerification()

            viewModel.consumeSignUpSuccess()
        }
    }

    SignUpContent(
        uiState = uiState,

        onUserIdChange = viewModel::updateUserId,
        onUserPwChange = viewModel::updateUserPw,
        onUserPwConfirmChange = viewModel::updateUserPwConfirm,

        onCheckIdClick = viewModel::checkIdDuplicate,
        onSubmitClick = viewModel::signUp
    )
}

@Composable
fun SignUpContent(
    uiState: SignUpUiState,

    onUserIdChange: (String) -> Unit,
    onUserPwChange: (String) -> Unit,
    onUserPwConfirmChange: (String) -> Unit,

    onCheckIdClick: () -> Unit,
    onSubmitClick: () -> Unit
){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ){

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ){
            //상단 바
            IssueissyuTopAppBar(
                titleText = "회원가입"
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 50.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ){
                Column(
                    modifier = Modifier
                        .padding(31.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ){
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        //아이디
                        CommonTextField(
                            label = "아이디",
                            placeholder = "이메일(아이디)을 입력하세요.",
                            value = uiState.userId,
                            onValueChange = onUserIdChange,
                            maxLength = 80
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "이메일(아이디) 입력 후 [중복 확인]을 눌러주세요. 서버에서 형식·중복을 검사합니다.",
                                    style = IssueTypo.Regular12.copy(
                                        color = if (uiState.userIdError != null) Issue else Gray_5,
                                    ),
                                )

                                Spacer(modifier = Modifier.height(5.dp))

                                if (uiState.userIdError != null) {
                                    Text(
                                        text = uiState.userIdError,
                                        style = IssueTypo.Regular12.copy(color = Issue),
                                    )
                                } else if (!uiState.isIdChecked && uiState.userId.isNotEmpty()) {
                                    Text(
                                        text = "아이디 중복을 확인해주세요",
                                        style = IssueTypo.Regular12.copy(color = Issue),
                                    )
                                }
                            }
                            //중복 확인 버튼
                            // 중복 확인 완료(확인됨)일 때만 비활성 — 아이디 변경 시 isIdChecked 리셋으로 다시 활성
                            Button(
                                onClick = onCheckIdClick,
                                enabled = uiState.userId.isNotBlank() &&
                                    !uiState.isIdChecking &&
                                    !uiState.isIdChecked,
                                modifier = Modifier.size(70.dp, 30.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandColor,
                                    disabledContainerColor = Gray_5
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(5.dp)
                            ){
                                Text(
                                    text = when {
                                        uiState.isIdChecking -> "확인중"
                                        uiState.isIdChecked -> "확인됨"
                                        else -> "중복 확인"
                                    },
                                    style = IssueTypo.Bold12.copy(color = White)
                                )
                            }
                        }

                    }

                    Column() {
                        //비번
                        CommonTextField(
                            label = "비밀번호",
                            placeholder = "비밀번호를 입력하세요.",
                            value = uiState.userPw,
                            onValueChange = onUserPwChange,
                            maxLength = 20,
                            isPassword = true
                        )

                        Spacer(modifier = Modifier.height(13.dp))

                        //비번 설명
                        Text(
                            text = "8~20자, 영문(대소문 무관)·숫자·특수문자를 각각 1자 이상 포함해주세요.",
                            modifier = Modifier
                                .padding(horizontal = 5.dp),
                            style = IssueTypo.Regular12.copy(
                                color = if (uiState.userPwError != null) Issue else Gray_5
                            )
                        )
                        uiState.userPwError?.let { err ->
                            Text(
                                text = err,
                                modifier = Modifier.padding(horizontal = 5.dp),
                                style = IssueTypo.Regular12.copy(color = Issue),
                            )
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ){
                        //비번 확인
                        CommonTextField(

                            label = "비밀번호 확인",
                            placeholder = "비밀번호를 입력하세요.",
                            value = uiState.userPwConfirm,
                            onValueChange = onUserPwConfirmChange,
                            maxLength = 20,
                            isPassword = true
                        )

                        uiState.userPwConfirmError?.let { err ->
                            Text(
                                text = err,
                                modifier = Modifier.padding(horizontal = 5.dp),
                                style = IssueTypo.Regular12.copy(color = Issue),
                            )
                        }
                    }
                }
                //하단 버튼
                Column(
                    modifier = Modifier.padding(horizontal = 31.dp)
                ) {
                    //캐릭터
                    Box(modifier = Modifier.fillMaxWidth()){
                        Image(
                            painter = painterResource(R.drawable.ic_character_1),
                            contentDescription = "캐릭터 배경",
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                    //작성 완료 버튼
                    CommonButton(
                        text = "작성 완료",
                        onClick = onSubmitClick,
                        isEnabled = uiState.canSubmit && !uiState.isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview(){
    SignUpContent(
        uiState = SignUpUiState(
            userId = "test@example.com",
            userPw = "Testpass1!",
            userPwConfirm = "Testpass1!",
            isIdChecked = true,
        ),
        onUserIdChange = {},
        onUserPwChange = {},
        onUserPwConfirmChange = {},
        onCheckIdClick = {},
        onSubmitClick = {}
    )
}