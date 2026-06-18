package com.issueissyu.fe.ui.navigation

import android.content.Context
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.issueissyu.fe.core.auth.AuthSessionState
import com.issueissyu.fe.core.notification.PushDestination
import com.issueissyu.fe.ui.navigation.AppDestinations.Onboarding.LOGIN_ROUTE
import com.issueissyu.fe.domain.model.notification.Notification
import com.issueissyu.fe.domain.model.notification.NotificationType
import com.issueissyu.fe.ui.screens.map.MapScreen
import com.issueissyu.fe.ui.screens.map.NotificationRoute
import com.issueissyu.fe.ui.screens.landing.LandingScreen
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
import com.issueissyu.fe.ui.screens.mypage.MySolverParticipationScreen
import com.issueissyu.fe.ui.screens.mypage.MYPAGE_REFRESH_KEY
import com.issueissyu.fe.ui.screens.mypage.MyPageEvent
import com.issueissyu.fe.ui.screens.mypage.MyPageScreen
import com.issueissyu.fe.ui.screens.mypage.MyPageTermScreen
import com.issueissyu.fe.ui.screens.mypage.ProfileChangeScreen
import com.issueissyu.fe.ui.screens.mypage.TermsType as MyPageTermsType
import com.issueissyu.fe.ui.screens.onboarding.TermsType as OnboardingTermsType
import kotlinx.coroutines.delay

@Composable
fun AppNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    onMapLocationSelectionModeChanged: (Boolean) -> Unit = {},
    pendingPush: PushDestination? = null,
    onPendingPushHandled: () -> Unit = {},
) {
    val sessionViewModel: AppSessionViewModel = hiltViewModel()
    val context = LocalContext.current
    val authState by sessionViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(pendingPush, authState) {
        val destination = pendingPush ?: return@LaunchedEffect
        if (authState != AuthSessionState.Authenticated) return@LaunchedEffect

        repeat(30) {
            val route = navController.currentDestination?.route
            if (canNavigateFromPush(route)) {
                navController.navigateToPushDestination(destination)
                onPendingPushHandled()
                return@LaunchedEffect
            }
            delay(100)
        }
    }

    LaunchedEffect(sessionViewModel, navController, context) {
        sessionViewModel.sessionExpiredMessages.collect { message ->
            if (!navController.currentDestination?.route.isLoginRoute()) {
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
                        route.isLoginRoute()
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
                onNavigateToLogin = { showStorageWarning ->
                    navController.navigateToLoginClearingBackStack(showStorageWarning)
                },
                onNavigateToOnboarding = {
                    navController.navigate(AppDestinations.Onboarding.TERM_ROUTE) {
                        popUpTo(AppDestinations.Onboarding.SPLASH_ROUTE) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = AppDestinations.Onboarding.LOGIN_ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument("storageWarning") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) { backStackEntry ->
            val showStorageWarning =
                backStackEntry.arguments?.getBoolean("storageWarning") ?: false
            LoginScreen(
                showStorageWarning = showStorageWarning,
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
                onNavigateToLanding = {
                    navController.navigate(AppDestinations.Onboarding.LANDING_ROUTE)
                },
            )
        }

        composable(AppDestinations.Onboarding.LANDING_ROUTE) {
            val previousRoute =
                navController.previousBackStackEntry?.destination?.route
            if (previousRoute != AppDestinations.MyPage.MYPAGE_ROUTE) {
                OnboardingBackDisabledHandler()
            }

            LandingScreen(
                onComplete = {
                    if (previousRoute == AppDestinations.MyPage.MYPAGE_ROUTE) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(AppDestinations.TOWN_ROUTE) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(AppDestinations.COLLECTION_ROUTE) {
            NavScreenWrapper(paddingValues = paddingValues) {
                CollectionScreen(
                    onNavigateToPinDetail = { pinId ->
                        navController.navigateToPinDetail(pinId)
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
                    onCommunityClick = { communityId, detailKind ->
                        navController.navigate(
                            AppDestinations.communityDetailRoute(
                                communityId = communityId,
                                kind = detailKind,
                            )
                        )
                    }
                )
            }
        }
        composable(
            route = AppDestinations.COMMUNITY_DETAIL_ROUTE,
            arguments = listOf(
                navArgument("communityId") {
                    type = NavType.LongType
                },
                navArgument("kind") {
                    type = NavType.StringType
                    defaultValue = ""
                },
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
                            MyPageEvent.NavigateToSolverParticipation -> {
                                navController.navigate(AppDestinations.MyPage.MY_SOLVER_PARTICIPATION_ROUTE)
                            }
                            MyPageEvent.NavigateToSettingAlarm -> {
                                navController.navigate(AppDestinations.MyPage.ALARM_SETTINGS_ROUTE)
                            }
                            MyPageEvent.NavigateToLanding -> {
                                navController.navigate(AppDestinations.Onboarding.LANDING_ROUTE)
                            }
                            MyPageEvent.NavigateToLogin -> {
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
                        navigateToFocusedPin(
                            navController = navController,
                            context = context,
                            pinId = pinId,
                        )
                    },
                )
            }
        }

        composable(AppDestinations.MyPage.MY_SOLVER_PARTICIPATION_ROUTE) {
            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true,
            ) {
                MySolverParticipationScreen(
                    onBackClick = { navController.navigateUp() },
                    onPinClick = { pinId ->
                        navigateToFocusedPin(
                            navController = navController,
                            context = context,
                            pinId = pinId,
                        )
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
        composable(AppDestinations.NOTIFICATION_ROUTE) {
            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true,
            ) {
                NotificationRoute(
                    onBack = { navController.popBackStack() },
                    onItemClick = { notification ->
                        navController.navigateFromNotification(notification)
                    },
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
                },
                navArgument("startHomeEdit") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            )
        ) { backStackEntry ->
            val pinId = backStackEntry.arguments?.getString("pinId").orEmpty()
            val startHomeEdit = backStackEntry.arguments?.getBoolean("startHomeEdit") ?: false

            NavScreenWrapper(
                paddingValues = paddingValues,
                removeTopPadding = true
            ) {
                PinDetailScreen(
                    pinId = pinId,
                    startHomeEdit = startHomeEdit,
                    savedStateHandle = backStackEntry.savedStateHandle,
                    onBackClick = { navController.popBackStack() },
                    onReportClick = { reportPinId ->
                        navController.navigate(AppDestinations.pinReportRoute(reportPinId))
                    },
                    onCommunityClick = { communityId ->
                        navController.navigate(AppDestinations.communityDetailRoute(communityId))
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
                    type = NavType.StringType
                    defaultValue = "0.0"
                },
                navArgument("pinLng") {
                    type = NavType.StringType
                    defaultValue = "0.0"
                },
                navArgument("userLat") {
                    type = NavType.StringType
                    defaultValue = "0.0"
                },
                navArgument("userLng") {
                    type = NavType.StringType
                    defaultValue = "0.0"
                },
                navArgument("address") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val pinType = backStackEntry.arguments?.getString("type")
            val pinLat = backStackEntry.arguments?.getString("pinLat")?.toDoubleOrNull() ?: 0.0
            val pinLng = backStackEntry.arguments?.getString("pinLng")?.toDoubleOrNull() ?: 0.0
            val userLat = backStackEntry.arguments?.getString("userLat")?.toDoubleOrNull() ?: 0.0
            val userLng = backStackEntry.arguments?.getString("userLng")?.toDoubleOrNull() ?: 0.0
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

private fun canNavigateFromPush(route: String?): Boolean {
    if (route == null) return false
    return route != AppDestinations.Onboarding.SPLASH_ROUTE && !route.isLoginRoute()
}

private fun String?.isLoginRoute(): Boolean =
    this?.startsWith(LOGIN_ROUTE) == true

private fun navigateToFocusedPin(
    navController: NavHostController,
    context: Context,
    pinId: String,
) {
    val id = pinId.toLongOrNull()
    if (id != null) {
        navController.navigate(AppDestinations.townRouteWithFocusPin(id)) {
            popUpTo(AppDestinations.TOWN_ROUTE) {
                inclusive = true
            }
        }
    } else {
        Toast.makeText(context, "핀 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
    }
}

private fun NavHostController.navigateToLoginClearingBackStack(
    showStorageWarning: Boolean = false,
) {
    navigate(AppDestinations.Onboarding.loginRoute(showStorageWarning)) {
        popUpTo(0) { inclusive = true }
    }
}

/** 지도 핀 카드·패치노트 등에서 핀 상세 화면으로 이동 */
fun NavHostController.navigateToPinDetail(pinId: String, startHomeEdit: Boolean = false) {
    if (pinId.isBlank()) return
    navigate(AppDestinations.pinDetailRoute(pinId, startHomeEdit))
}

fun NavHostController.navigateToPushDestination(destination: PushDestination) {
    when (destination) {
        is PushDestination.PinDetail -> navigateToPinDetail(destination.pinId)
        is PushDestination.CommunityDetail ->
            navigate(AppDestinations.communityDetailRoute(destination.communityId))
    }
}

/** 알람 목록 탭 → 상세 (LIKE → 핀, HOT/EVENT/STORE → 커뮤니티) */
fun NavHostController.navigateFromNotification(notification: Notification) {
    val destination = when (notification.type) {
        NotificationType.LIKE -> PushDestination.PinDetail(notification.pinId.toString())
        NotificationType.HOT,
        NotificationType.EVENT,
        NotificationType.STORE -> notification.communityId?.let { PushDestination.CommunityDetail(it) }
    } ?: return
    navigateToPushDestination(destination)
}
