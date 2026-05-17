package com.issueissyu.fe.ui.viewmodels

//로그인 성공 후 화면
sealed interface LoginEffect {
    data object NavigateToTerms : LoginEffect
    data object NavigateToMain : LoginEffect
}
