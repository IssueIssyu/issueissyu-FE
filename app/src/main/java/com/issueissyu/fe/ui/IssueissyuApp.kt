package com.issueissyu.fe.ui

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.issueissyu.fe.ui.components.BottomNavigationBar
import com.issueissyu.fe.ui.navigation.AppDestinations
import com.issueissyu.fe.ui.navigation.AppNavGraph

/** 지도·패치노트·핀 생성/상세 등은 동네 탭과 같은 흐름으로 간주해 선택 상태만 맞춥니다. */
private fun bottomNavHighlightRoute(route: String?): String? {
    if (route == null) return null
    if (route == AppDestinations.TOWN_ROUTE ||
        route == AppDestinations.PATCH_NOTE_ROUTE ||
        route.startsWith("${AppDestinations.PIN_CREATION_ROUTE}?") ||
        route == AppDestinations.PIN_CREATION_ROUTE ||
        route.startsWith("pin_detail_route") ||
        route.startsWith("pin_edit_route") ||
        route.startsWith("pin_report_route")
    ) {
        return AppDestinations.TOWN_ROUTE
    }
    if (route == AppDestinations.MyPage.MY_ISSUES_ROUTE ||
        route == AppDestinations.MyPage.ALARM_SETTINGS_ROUTE ||
        route == AppDestinations.MyPage.PROFILE_CHANGE_ROUTE ||
        route == AppDestinations.MyPage.LOCAL_CHANGE_ROUTE ||
        route == AppDestinations.MyPage.TERMS_ROUTE
    ) {
        return AppDestinations.MyPage.MYPAGE_ROUTE
    }
    return route
}

private fun shouldShowBottomBar(route: String?): Boolean {
    if (route == null) return false
    
    // 온보딩, 로그인, 인증, 약관, 동네설정 등 초기 진입/설정 화면에서는 숨김
    val isOnboardingOrAuth = route == AppDestinations.Onboarding.SPLASH_ROUTE ||
            route == AppDestinations.Onboarding.LANDING_ROUTE ||
            route == AppDestinations.Onboarding.LOGIN_ROUTE ||
            route == AppDestinations.Onboarding.SIGNUP_ROUTE ||
            route == AppDestinations.Onboarding.TERM_ROUTE ||
            route == AppDestinations.Onboarding.TERM_DETAIL_ROUTE ||
            route == AppDestinations.Onboarding.USER_VERIFICATION_ROUTE ||
            route == AppDestinations.Onboarding.LOCAL_VERIFICATION_ROUTE ||
            route == AppDestinations.Onboarding.COMPLETE_ROUTE ||
            route == AppDestinations.MyPage.LOCAL_CHANGE_ROUTE ||
            route == AppDestinations.MyPage.TERMS_ROUTE

    if (isOnboardingOrAuth) return false

    return route == AppDestinations.HOME_ROUTE ||
        route == AppDestinations.COLLECTION_ROUTE ||
        route == AppDestinations.TOWN_ROUTE ||
        route == AppDestinations.COMMUNITY_ROUTE ||
        route == AppDestinations.MyPage.MYPAGE_ROUTE ||
        route == AppDestinations.PATCH_NOTE_ROUTE ||
        route.startsWith("${AppDestinations.PIN_CREATION_ROUTE}?") ||
        route == AppDestinations.PIN_CREATION_ROUTE ||
        route.startsWith("pin_detail_route") ||
        route.startsWith("pin_edit_route") ||
        route.startsWith("pin_report_route") ||
        route == AppDestinations.MyPage.MY_ISSUES_ROUTE ||
        route == AppDestinations.MyPage.ALARM_SETTINGS_ROUTE ||
        route == AppDestinations.MyPage.PROFILE_CHANGE_ROUTE
}

@Composable
fun App() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val highlightRoute = remember(currentRoute) { bottomNavHighlightRoute(currentRoute) }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    highlightRoute = highlightRoute
                )
            }
        }
    ) { paddingValues ->
        AppNavGraph(
            navController = navController,
            paddingValues = paddingValues
        )
    }
}
