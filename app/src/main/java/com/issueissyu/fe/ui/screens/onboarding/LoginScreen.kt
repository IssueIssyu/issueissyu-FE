package com.issueissyu.fe.ui.screens.onboarding

import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.issueissyu.fe.BuildConfig
import com.issueissyu.fe.R
import com.issueissyu.fe.core.auth.SessionManager
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.issueissyu.fe.ui.components.CommonButton
import com.issueissyu.fe.ui.components.CommonTextField
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Text
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White
import com.issueissyu.fe.ui.theme.suiteFontFamily

@Composable
fun LoginScreen(
    showStorageWarning: Boolean = false,
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: (isNew: Boolean) -> Unit,
    onNavigateToSignUp: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val onLoginSuccessUpdated by rememberUpdatedState(onLoginSuccess)
    LaunchedEffect(showStorageWarning) {
        if (showStorageWarning) {
            Toast.makeText(
                context,
                SessionManager.STORAGE_UNAVAILABLE_MESSAGE,
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LoginEffect.NavigateToTerms -> onLoginSuccessUpdated(true)
                LoginEffect.NavigateToMain -> onLoginSuccessUpdated(false)
            }
        }
    }

    LoginContent(
        userId = uiState.userId,
        userPw = uiState.userPw,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onUserIdChange = viewModel::updateUserId,
        onUserPwChange = viewModel::updatePassword,
        onLocalClick = viewModel::localLogin,
        onNaverClick = {
            when {
                uiState.isLoading -> Unit
                BuildConfig.NAVER_CLIENT_ID.isBlank() -> {
                    viewModel.reportNaverLoginError(
                        "NAVER_CLIENT_ID가 비어 있습니다. local.properties를 확인하세요.",
                    )
                }
                else -> {
                    val activity = context.findFragmentActivityForNaver()
                    if (activity == null) {
                        viewModel.reportNaverLoginError(
                            "로그인 화면(Activity)을 찾지 못했습니다. 앱을 다시 실행해 보세요.",
                        )
                    } else {
                        NaverIdLoginSDK.authenticate(
                            activity,
                            object : OAuthLoginCallback {
                                override fun onSuccess() {
                                    val access = NaverIdLoginSDK.getAccessToken().orEmpty()
                                    val refresh = NaverIdLoginSDK.getRefreshToken().orEmpty()
                                    viewModel.naverLogin(access, refresh)
                                }

                                override fun onFailure(httpStatus: Int, message: String) {
                                    viewModel.reportNaverLoginError(
                                        message.ifBlank { "네이버 로그인 실패 ($httpStatus)" },
                                    )
                                }

                                override fun onError(errorCode: Int, message: String) {
                                    viewModel.reportNaverLoginError(
                                        message.ifBlank { "네이버 로그인 오류 ($errorCode)" },
                                    )
                                }
                            },
                        )
                    }
                }
            }
        },
        onSignUpClick = onNavigateToSignUp
    )
}


@Composable
fun LoginContent(
    userId: String,
    userPw: String,
    isLoading: Boolean,
    errorMessage: String?,
    onUserIdChange: (String) -> Unit,
    onUserPwChange: (String) -> Unit,
    onLocalClick: () -> Unit,
    onNaverClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val canLogin = userId.isNotBlank() && userPw.isNotBlank() && !isLoading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 31.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            //가장 편한 방법
            Text(
                text = "가장 편한 방법으로\n시작해 보세요!",
                style = IssueTypo.Bold18.copy(color = Title, fontSize = 24.sp),
                lineHeight = 33.sp
            )
            //1분 회원가입 가능
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = BrandColor)) {
                        append("1분")
                    }
                    append("이면 회원가입 가능해요")
                },
                fontFamily = suiteFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Text
            )
            //로컬 로그인
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //아이디
                Column() {
                    Image(
                        painter = painterResource(R.drawable.ic_half_character),
                        contentDescription = "캐릭터",
                        modifier = Modifier.align(Alignment.End)
                    )
                    CommonTextField(
                        value = userId,
                        onValueChange = onUserIdChange,
                        placeholder = "이메일(아이디) 입력",
                        maxLength = 80,
                    )
                }
                //비번
                CommonTextField(
                    value = userPw,
                    onValueChange = onUserPwChange,
                    placeholder = "비밀번호 입력",
                    maxLength = 20,
                    isPassword = true
                )
                //로그인 버튼
                CommonButton(
                    text = "로그인",
                    onClick = onLocalClick,
                    modifier = Modifier.fillMaxWidth(),
                    isEnabled = canLogin
                )
                //에러 메세지 (비활성화 디폴트)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            style = IssueTypo.Regular12.copy(color = Issue)
                        )
                    }
                }
            }

            //또는
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Gray_5
                )
                Text(
                    text = "또는",
                    style = IssueTypo.Regular12.copy(color = Gray_5)
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Gray_5
                )
            }
            //소셜
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ){
                Image(
                    painter = painterResource(R.drawable.img_login_naver),
                    contentDescription = "네이버 로그인",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable(enabled = !isLoading) {
                            onNaverClick()
                        },
                    contentScale = ContentScale.FillWidth
                )
            }
            //회원가입
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                        color = BrandColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                        )
                    ){
                        append("회원가입 ")
                    }
                    append("하러 가기")
                },
                fontFamily = suiteFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Text,
                modifier = Modifier.clickable {
                    onSignUpClick()
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview(){
    LoginContent(
        userId = "",
        userPw = "",
        isLoading = true,
        errorMessage = "",
        onUserIdChange = {},
        onUserPwChange = {},
        onLocalClick = {},
        onNaverClick = {},
        onSignUpClick = {}
    )
}

private fun Context.findFragmentActivityForNaver(): FragmentActivity? {
    var current: Context = this
    while (true) {
        when (current) {
            is FragmentActivity -> return current
            is ContextWrapper -> {
                val next = current.baseContext
                if (next === current) return null
                current = next
            }
            else -> return null
        }
    }
}
