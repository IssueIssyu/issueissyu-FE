package com.issueissyu.fe.ui.navigation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.issueissyu.fe.core.auth.AuthSessionState
import com.issueissyu.fe.ui.navigation.AppDestinations.Onboarding.LOGIN_ROUTE
import com.issueissyu.fe.ui.screens.map.MapScreen
import com.issueissyu.fe.ui.screens.onboarding.CompleteScreen
import com.issueissyu.fe.ui.screens.onboarding.LocalVerificationScreen
import com.issueissyu.fe.ui.screens.onboarding.LoginScreen
import com.issueissyu.fe.ui.screens.onboarding.SignUpScreen
import com.issueissyu.fe.ui.screens.onboarding.SplashScreen
import com.issueissyu.fe.ui.screens.onboarding.TermDetailScreen
import com.issueissyu.fe.ui.screens.onboarding.TermScreen
import com.issueissyu.fe.ui.screens.onboarding.TermsType
import com.issueissyu.fe.ui.screens.onboarding.UserVerificationScreen
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.ui.screens.patchnote.PatchNotesRoute
import com.issueissyu.fe.ui.screens.pincreate.PinCreateScreen
import com.issueissyu.fe.ui.screens.pindetail.PinDetailScreen
import com.issueissyu.fe.ui.screens.pindetail.PIN_DETAIL_REFRESH_KEY
import com.issueissyu.fe.ui.screens.pindetail.PinReportScreen
import com.issueissyu.fe.ui.screens.pindetail.ReportTargetType
import com.issueissyu.fe.ui.screens.collection.CollectionScreen
import com.issueissyu.fe.ui.screens.community.CommunityScreen
import com.issueissyu.fe.ui.screens.community.detail.CommunityDetailScreen
import com.issueissyu.fe.ui.screens.community.detail.COMMUNITY_DETAIL_REFRESH_KEY
import com.issueissyu.fe.ui.screens.map.PIN_CREATE_FOCUS_PIN_ID_KEY
import com.issueissyu.fe.ui.screens.map.PIN_CREATE_MAP_REFRESH_KEY
import com.issueissyu.fe.ui.screens.mypage.AlarmSettingScreen
import com.issueissyu.fe.ui.screens.mypage.ChangeLocalScreen
import com.issueissyu.fe.ui.screens.mypage.MyIssueScreen
import com.issueissyu.fe.ui.screens.mypage.MYPAGE_REFRESH_KEY
import com.issueissyu.fe.ui.screens.mypage.MyPageEvent
import com.issueissyu.fe.ui.screens.mypage.MyPageScreen
import com.issueissyu.fe.ui.screens.mypage.MyPageTermScreen
import com.issueissyu.fe.ui.screens.mypage.ProfileChangeScreen
import com.issueissyu.fe.ui.screens.mypage.TermsType as MyPageTermsType
import com.issueissyu.fe.ui.screens.onboarding.TermsType as OnboardingTermsType

@Composable
fun AppNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    onMapLocationSelectionModeChanged: (Boolean) -> Unit = {},
) {
    val sessionViewModel: AppSessionViewModel = hiltViewModel()
    val context = LocalContext.current

    LaunchedEffect(sessionViewModel, navController, context) {
        sessionViewModel.sessionExpiredMessages.collect { message ->
            if (navController.currentDestination?.route != LOGIN_ROUTE) {
                navController.navigateToLoginClearingBackStack()
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(sessionViewModel, navController) {
        // SessionManager는 토큰이 있으면 기동 시 Authenticated. 스플래시→메인은 markAuthenticated()를
        // 호출하지 않아도 되지만, collect 전 만료 등에 대비해 현재 상태로 시드한다.
        var wasAuthenticated =
            sessionViewModel.authState.value == AuthSessionState.Authenticated
        sessionViewModel.authState.collect { state ->
            when (state) {
                AuthSessionState.Authenticated -> wasAuthenticated = true
                AuthSessionState.Unauthenticated -> {
                    if (!wasAuthenticated) return@collect
                    val route = navController.currentDestination?.route ?: return@collect
                    if (
                        route == AppDestinations.Onboarding.SPLASH_ROUTE ||
                        route == LOGIN_ROUTE
                    ) {
                        return@collect
                    }
                    navController.navigateToLoginClearingBackStack()
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestinations.Onboarding.SPLASH_ROUTE
    ) {
        composable(AppDestinations.Onboarding.SPLASH_ROUTE) {
            SplashScreen(
                viewModel = hiltViewModel(),
                onNavigateToMain = {
                    navController.navigate(AppDestinations.TOWN_ROUTE) {
                        popUpTo(AppDestinations.Onboarding.SPLASH_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigateToLoginClearingBackStack()
                },
                onNavigateToOnboarding = {
                    navController.navigate(AppDestinations.Onboarding.TERM_ROUTE) {
                        popUpTo(AppDestinations.Onboarding.SPLASH_ROUTE) { inclusive = true }
                    }
                },
            )
        }

        composable(LOGIN_ROUTE) {
            LoginScreen(
                viewModel = hiltViewModel(),
                onLoginSuccess = { isNewUser ->
                    if (isNewUser) {
                        navController.navigate(AppDestinations.Onboarding.TERM_ROUTE) {
                            popUpTo(AppDestinations.Onboarding.LOGIN_ROUTE) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(AppDestinations.TOWN_ROUTE) {
                            popUpTo(AppDestinations.Onboarding.LOGIN_ROUTE) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(AppDestinations.Onboarding.SIGNUP_ROUTE)
                }
            )
        }

        composable(AppDestinations.Onboarding.SIGNUP_ROUTE) {
            BackHandler {
                navController.navigateToLoginClearingBackStack()
            }

            SignUpScreen(
                viewModel = hiltViewModel(),
                onSignUpCompleteNavigateToLogin = {
                    navController.navigateToLoginClearingBackStack()
                },
            )
        }

        composable(AppDestinations.Onboarding.TERM_ROUTE) {
            OnboardingBackDisabledHandler()

            TermScreen(
                onAgreeClick = { navController.navigate(AppDestinations.Onboarding.USER_VERIFICATION_ROUTE) },
                onTermsDetailClick = { termsType ->
                    navController.navigate(
                        AppDestinations.Onboarding.termDetailRoute(termsType.name)
                    )
                }
            )
        }

        composable(
            route = AppDestinations.Onboarding.TERM_DETAIL_ROUTE,
            arguments = listOf(
                navArgument("termsType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val termsType = backStackEntry.arguments?.getString("termsType")?.let {
                TermsType.valueOf(it)
            } ?: TermsType.SERVICE

            TermDetailScreen(
                termsType = termsType,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(AppDestinations.Onboarding.USER_VERIFICATION_ROUTE) {
            OnboardingBackDisabledHandler()

            UserVerificationScreen(
                onVerificationComplete = { _, _, _ ->
                    navController.navigate(AppDestinations.Onboarding.LOCAL_VERIFICATION_ROUTE)
                },
                onNavigateToLogin = {
                    navController.navigateToLoginClearingBackStack()
                },
            )
        }

        composable(AppDestinations.Onboarding.LOCAL_VERIFICATION_ROUTE) {
            OnboardingBackDisabledHandler()

            LocalVerificationScreen(
                onCompleteRegisterClick = { navController.navigate(AppDestinations.Onboarding.COMPLETE_ROUTE) }
            )
        }

        composable(AppDestinations.Onboarding.COMPLETE_ROUTE) {
            OnboardingBackDisabledHandler()

            CompleteScreen(
                onNavigateToMain = {
                    navController.navigate(AppDestinations.TOWN_ROUTE) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLanding = {}
            )
        }
        composable(AppDestinations.COLLECTION_ROUTE) {
            NavScreenWrapper(paddingValues = paddingValues) {
                CollectionScreen(
                    onNavigateToNoticeDetail = { notice ->
                        notice.toLongOrNull()?.let { communityId ->
                            navController.navigate(AppDestinations.communityDetailRoute(communityId))
                        }
                    },
                )
            }
        }
        composable(
            route = AppDestinations.TOWN_ROUTE_WITH_FOCUS_PIN,
            arguments = listOf(
                navArgument("focusPinId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true
            ) {
                MapScreen(
                    navController = navController,
                    focusPinId = backStackEntry.arguments?.getString("focusPinId"),
                    savedStateHandle = backStackEntry.savedStateHandle,
                    onLocationSelectionModeChanged = onMapLocationSelectionModeChanged,
                )
            }
        }
        composable(AppDestinations.COMMUNITY_ROUTE) {
            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true
            ) {
                CommunityScreen(
                    onBackClick = {
                        navController.navigateUp()
                    },
                    onCommunityClick = { communityId ->
                        navController.navigate(AppDestinations.communityDetailRoute(communityId))
                    }
                )
            }
        }
        composable(
            route = AppDestinations.COMMUNITY_DETAIL_ROUTE,
            arguments = listOf(
                navArgument("communityId") {
                    type = NavType.LongType
                }
            )
        ) {
            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true
            ) {
                CommunityDetailScreen(
                    savedStateHandle = it.savedStateHandle,
                    onBackClick = {
                        navController.navigateUp()
                    },
                    onMapClick = { pinId ->
                        navController.navigate(AppDestinations.townRouteWithFocusPin(pinId)) {
                            popUpTo(AppDestinations.TOWN_ROUTE) {
                                inclusive = true
                            }
                        }
                    },
                    onReportClick = { communityId ->
                        navController.navigate(AppDestinations.communityReportRoute(communityId))
                    }
                )
            }
        }
        composable(
            route = AppDestinations.COMMUNITY_REPORT_ROUTE,
            arguments = listOf(
                navArgument("communityId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val communityId = backStackEntry.arguments?.getLong("communityId") ?: return@composable

            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true
            ) {
                PinReportScreen(
                    targetId = communityId.toString(),
                    targetType = ReportTargetType.COMMUNITY,
                    onBackClick = { navController.popBackStack() },
                    onSuccess = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(COMMUNITY_DETAIL_REFRESH_KEY, true)
                        navController.popBackStack()
                    }
                )
            }
        }
        composable(AppDestinations.MyPage.MYPAGE_ROUTE) { backStackEntry ->
            NavScreenWrapper(paddingValues = paddingValues) {
                MyPageScreen(
                    modifier = Modifier,
                    savedStateHandle = backStackEntry.savedStateHandle,
                    onEvent = { event ->
                        when (event) {
                            MyPageEvent.NavigateBack -> navController.navigateUp()
                            MyPageEvent.NavigateToProfile -> {
                                navController.navigate(AppDestinations.MyPage.PROFILE_CHANGE_ROUTE)
                            }
                            MyPageEvent.NavigateToLocal -> {
                                navController.navigate(AppDestinations.MyPage.LOCAL_CHANGE_ROUTE)
                            }
                            MyPageEvent.NavigateToIssue -> {
                                navController.navigate(AppDestinations.MyPage.MY_ISSUES_ROUTE)
                            }
                            MyPageEvent.NavigateToSettingAlarm -> {
                                navController.navigate(AppDestinations.MyPage.ALARM_SETTINGS_ROUTE)
                            }
                            MyPageEvent.NavigateToLanding -> {
                                navController.navigateToLoginClearingBackStack()
                            }
                            MyPageEvent.NavigateToTerm -> {
                                navController.navigate(AppDestinations.MyPage.TERMS_ROUTE)
                            }
                        }
                    },
                )
            }
        }

        composable(AppDestinations.MyPage.PROFILE_CHANGE_ROUTE) {
            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true,
            ) {
                ProfileChangeScreen(
                    onBackClick = { refreshMyPage ->
                        if (refreshMyPage) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(MYPAGE_REFRESH_KEY, true)
                        }
                        navController.navigateUp()
                    },
                    onCollectionClick = { navController.navigate(AppDestinations.COLLECTION_ROUTE) },
                    onCompleteClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(MYPAGE_REFRESH_KEY, true)
                        navController.navigateUp()
                    },
                )
            }
        }

        composable(AppDestinations.MyPage.TERMS_ROUTE) {
            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true,
            ) {
                MyPageTermScreen(
                    onBackClick = { navController.navigateUp() },
                    onTermsDetailClick = { termsType ->
                        val onboardingTermsType = when (termsType) {
                            MyPageTermsType.SERVICE -> OnboardingTermsType.SERVICE
                            MyPageTermsType.PRIVACY -> OnboardingTermsType.PRIVACY
                            MyPageTermsType.LOCATION -> OnboardingTermsType.LOCATION
                        }
                        navController.navigate(
                            AppDestinations.Onboarding.termDetailRoute(onboardingTermsType.name),
                        )
                    },
                )
            }
        }

        composable(AppDestinations.MyPage.MY_ISSUES_ROUTE) {
            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true,
            ) {
                MyIssueScreen(
                    onBackClick = { navController.navigateUp() },
                    onPinClick = { pinId ->
                        pinId.toLongOrNull()?.let { id ->
                            navController.navigate(AppDestinations.townRouteWithFocusPin(id)) {
                                popUpTo(AppDestinations.TOWN_ROUTE) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                )
            }
        }

        composable(AppDestinations.MyPage.ALARM_SETTINGS_ROUTE) {
            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true,
            ) {
                AlarmSettingScreen(
                    onBackClick = { navController.navigateUp() },
                )
            }
        }

        composable(AppDestinations.MyPage.LOCAL_CHANGE_ROUTE) {
            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true,
            ) {
                ChangeLocalScreen(
                    onBackClick = { navController.navigateUp() },
                    onComplete = { navController.navigateUp() },
                )
            }
        }
        composable(AppDestinations.PATCH_NOTE_ROUTE) {
            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true
            ) {
                PatchNotesRoute(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onPatchNoteClick = { pinId ->
                        navController.navigateToPinDetail(pinId)
                    }
                )
            }
        }
        composable(
            route = AppDestinations.PIN_DETAIL_ROUTE,
            arguments = listOf(
                navArgument("pinId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val pinId = backStackEntry.arguments?.getString("pinId").orEmpty()

            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true
            ) {
                PinDetailScreen(
                    pinId = pinId,
                    savedStateHandle = backStackEntry.savedStateHandle,
                    onBackClick = { navController.popBackStack() },
                    onReportClick = { reportPinId ->
                        navController.navigate(AppDestinations.pinReportRoute(reportPinId))
                    },
                )
            }
        }
        composable(
            route = AppDestinations.PIN_REPORT_ROUTE,
            arguments = listOf(
                navArgument("pinId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val pinId = backStackEntry.arguments?.getString("pinId").orEmpty()

            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true
            ) {
                PinReportScreen(
                    targetId = pinId,
                    targetType = ReportTargetType.PIN,
                    onBackClick = { navController.popBackStack() },
                    onSuccess = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(PIN_DETAIL_REFRESH_KEY, true)
                        navController.popBackStack()
                    }
                )
            }
        }
        composable(
            route = "${AppDestinations.PIN_CREATION_ROUTE}?type={type}&pinLat={pinLat}&pinLng={pinLng}&userLat={userLat}&userLng={userLng}&address={address}",
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "issue"
                },
                navArgument("pinLat") {
                    type = NavType.FloatType
                    defaultValue = 0f
                },
                navArgument("pinLng") {
                    type = NavType.FloatType
                    defaultValue = 0f
                },
                navArgument("userLat") {
                    type = NavType.FloatType
                    defaultValue = 0f
                },
                navArgument("userLng") {
                    type = NavType.FloatType
                    defaultValue = 0f
                },
                navArgument("address") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val pinType = backStackEntry.arguments?.getString("type")
            // route는 Float로 정의되어 있어 Double로 변환해 PinCreateScreen에 넘긴다.
            // TODO: NavType.Float → 사용자 정의 NavType 또는 String 인코딩으로 정밀도 손실을 줄이는 방안 검토.
            val pinLat = (backStackEntry.arguments?.getFloat("pinLat") ?: 0f).toDouble()
            val pinLng = (backStackEntry.arguments?.getFloat("pinLng") ?: 0f).toDouble()
            val userLat = (backStackEntry.arguments?.getFloat("userLat") ?: 0f).toDouble()
            val userLng = (backStackEntry.arguments?.getFloat("userLng") ?: 0f).toDouble()
            val address = Uri.decode(backStackEntry.arguments?.getString("address").orEmpty())

            // 일반 유저 생성 대상은 ISSUE / COMMUNICATION 두 가지. 그 외는 화면 진입을 차단한다.
            val category = when (pinType?.lowercase()) {
                "issue" -> PinCategory.ISSUE
                "communication" -> PinCategory.COMMUNICATION
                else -> null
            }

            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true
            ) {
                if (category == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("지원하지 않는 핀 종류입니다: ${pinType.orEmpty()}")
                    }
                } else {
                    PinCreateScreen(
                        category = category,
                        pinLat = pinLat,
                        pinLng = pinLng,
                        userLat = userLat,
                        userLng = userLng,
                        address = address,
                        onBackClick = { navController.popBackStack() },
                        onCreated = { pinId ->
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(PIN_CREATE_MAP_REFRESH_KEY, true)
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(PIN_CREATE_FOCUS_PIN_ID_KEY, pinId)
                            navController.popBackStack()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NavScreenWrapper(
    paddingValues: PaddingValues,
    removeTopPadding: Boolean = false,
    content: @Composable () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val appliedPadding = if (removeTopPadding) {
        PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            top = 0.dp,
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = paddingValues.calculateBottomPadding()
        )
    } else {
        paddingValues
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(appliedPadding)
    ) {
        content()
    }
}

//시스템 뒤로가기 -> 막힘
@Composable
private fun OnboardingBackDisabledHandler() {
    BackHandler { }
}

private fun NavHostController.navigateToLoginClearingBackStack() {
    navigate(LOGIN_ROUTE) {
        popUpTo(0) { inclusive = true }
    }
}

/** 지도 핀 카드·패치노트 등에서 핀 상세 화면으로 이동 */
fun NavHostController.navigateToPinDetail(pinId: String) {
    if (pinId.isBlank()) return
    navigate(AppDestinations.pinDetailRoute(pinId))
}
