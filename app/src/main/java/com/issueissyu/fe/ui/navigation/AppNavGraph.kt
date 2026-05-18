package com.issueissyu.fe.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.issueissyu.fe.ui.navigation.AppDestinations.Onboarding.LOGIN_ROUTE
import com.issueissyu.fe.ui.screens.home.HomeScreen
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
import com.issueissyu.fe.data.model.PinCategory
import com.issueissyu.fe.ui.screens.patchnote.PatchNotesRoute
import com.issueissyu.fe.ui.screens.pincreate.PinCreateScreen
import com.issueissyu.fe.ui.screens.pindetail.PinDetailScreen
import com.issueissyu.fe.ui.screens.pindetail.PinReportScreen
import com.issueissyu.fe.ui.screens.community.CommunityScreen
import com.issueissyu.fe.ui.screens.community.detail.CommunityDetailScreen
import com.issueissyu.fe.ui.screens.collection.CollectionScreen
import com.issueissyu.fe.ui.screens.mypage.MyPageEvent
import com.issueissyu.fe.ui.screens.mypage.MyPageScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    val layoutDirection = LocalLayoutDirection.current
    val navigationPaddingValues = PaddingValues(
        start = paddingValues.calculateStartPadding(layoutDirection),
        top = 0.dp,
        end = paddingValues.calculateEndPadding(layoutDirection),
        bottom = paddingValues.calculateBottomPadding()
    )

    NavHost(
        navController = navController,
        startDestination = AppDestinations.TOWN_ROUTE
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

        composable(AppDestinations.HOME_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(navigationPaddingValues)
            ) {
                HomeScreen()
            }
        }
        composable(AppDestinations.COLLECTION_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(navigationPaddingValues)
            ) {
                CollectionScreen()
            }
        }
        composable(AppDestinations.TOWN_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(navigationPaddingValues)
            ) {
                MapScreen(navController = navController)
            }
        }
        composable(AppDestinations.COMMUNITY_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(navigationPaddingValues)
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(navigationPaddingValues)
            ) {
                CommunityDetailScreen(
                    onBackClick = {
                        navController.navigateUp()
                    }
                )
            }
        }
        composable(AppDestinations.MYPAGE_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(navigationPaddingValues)
            ) {
                MyPageScreen(
                    onEvent = { event ->
                        when (event) {
                            MyPageEvent.NavigateBack -> navController.navigateUp()
                            MyPageEvent.NavigateToLanding -> {
                                navController.navigate(AppDestinations.Onboarding.LOGIN_ROUTE)
                            }
                            MyPageEvent.NavigateToProfile,
                            MyPageEvent.NavigateToLocal,
                            MyPageEvent.NavigateToIssue,
                            MyPageEvent.NavigateToSettingAlarm,
                            MyPageEvent.NavigateToTerm,
                            MyPageEvent.Logout,
                            MyPageEvent.Withdraw -> {
                                // TODO: 마이페이지 하위 라우트 정의 후 연결
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        composable(AppDestinations.PATCH_NOTE_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(navigationPaddingValues)
            ) {
                PatchNotesRoute(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onPatchNoteClick = { _ ->
                        // TODO: 핀 상세 화면 route 확정 후 pinId 기반으로 navigate
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(navigationPaddingValues)
            ) {
                PinDetailScreen(
                    pinId = pinId,
                    onBackClick = { navController.popBackStack() },
                    onReportClick = { reportPinId ->
                        navController.navigate(AppDestinations.pinReportRoute(reportPinId))
                    }
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(navigationPaddingValues)
            ) {
                PinReportScreen(
                    pinId = pinId,
                    onBackClick = { navController.popBackStack() },
                    onSubmitClick = { _ ->
                        // TODO: 신고 API 연결 (PinReportRepository.reportPin 등)
                        // TODO: 성공 시 신고 완료 Dialog 노출 후 popBackStack
                        navController.popBackStack()
                    }
                )
            }
        }
        composable(
            route = "${AppDestinations.PIN_CREATION_ROUTE}?type={type}&pinLat={pinLat}&pinLng={pinLng}&userLat={userLat}&userLng={userLng}",
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

            // 일반 유저 생성 대상은 ISSUE / COMMUNICATION 두 가지. 그 외는 화면 진입을 차단한다.
            val category = when (pinType?.lowercase()) {
                "issue" -> PinCategory.ISSUE
                "communication" -> PinCategory.COMMUNICATION
                else -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(navigationPaddingValues)
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
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
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
