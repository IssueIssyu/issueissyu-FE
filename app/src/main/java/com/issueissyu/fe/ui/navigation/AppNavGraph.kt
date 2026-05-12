package com.issueissyu.fe.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun AppNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.TOWN_ROUTE
    ) {
        composable(AppDestinations.Onboarding.SPLASH_ROUTE) {
            SplashScreen(
                viewModel = hiltViewModel(),
                onNavigateToMain = {
                    navController.navigate(AppDestinations.HOME_ROUTE) {
                        popUpTo(AppDestinations.Onboarding.SPLASH_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(LOGIN_ROUTE) {
                        popUpTo(AppDestinations.Onboarding.SPLASH_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(LOGIN_ROUTE) {
            LoginScreen(
                viewModel = hiltViewModel(),
                onNavigateToMain = {
                    navController.navigate(AppDestinations.HOME_ROUTE) {
                        popUpTo(LOGIN_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(AppDestinations.Onboarding.SIGNUP_ROUTE)
                }
            )
        }

        composable(AppDestinations.Onboarding.SIGNUP_ROUTE) {
            BackHandler {
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(0) { inclusive = true }
                }
            }

            SignUpScreen(
                viewModel = hiltViewModel(),
                onNavigateToVerification = {
                    navController.navigate(AppDestinations.Onboarding.TERM_ROUTE)
                }
            )
        }

        composable(AppDestinations.Onboarding.TERM_ROUTE) {
            BackHandler {
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(0) { inclusive = true }
                }
            }

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
            BackHandler {
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(0) { inclusive = true }
                }
            }

            UserVerificationScreen(
                onVerificationComplete = { _, _, _ ->
                    navController.navigate(AppDestinations.Onboarding.LOCAL_VERIFICATION_ROUTE)
                }
            )
        }

        composable(AppDestinations.Onboarding.LOCAL_VERIFICATION_ROUTE) {
            BackHandler {
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(0) { inclusive = true }
                }
            }

            LocalVerificationScreen(
                onCompleteRegisterClick = { navController.navigate(AppDestinations.Onboarding.COMPLETE_ROUTE) }
            )
        }

        composable(AppDestinations.Onboarding.COMPLETE_ROUTE) {
            BackHandler {
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(0) { inclusive = true }
                }
            }

            CompleteScreen(
                onNavigateToMain = {
                    navController.navigate(AppDestinations.HOME_ROUTE) {
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
                    .padding(paddingValues)
            ) {
                HomeScreen()
            }
        }
        composable(AppDestinations.COLLECTION_ROUTE) { /* TODO: CollectionScreen */ }
        composable(AppDestinations.TOWN_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MapScreen(navController = navController)
            }
        }
        composable(AppDestinations.COMMUNITY_ROUTE) { /* TODO: CommunityScreen */ }
        composable(AppDestinations.MYPAGE_ROUTE) { /* TODO: MypageScreen */ }
        composable(AppDestinations.PATCH_NOTE_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                    .padding(paddingValues)
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
                    .padding(paddingValues)
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
                    .padding(paddingValues)
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
                        onBackClick = { navController.popBackStack() },
                        onCreated = {
                            // TODO: 실제 생성 API 연결 후 생성된 pinId 기반 상세 화면 이동 또는 지도 새로고침 처리.
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
