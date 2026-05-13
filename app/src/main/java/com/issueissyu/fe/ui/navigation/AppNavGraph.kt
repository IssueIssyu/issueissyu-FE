package com.issueissyu.fe.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
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
import com.issueissyu.fe.ui.screens.pinedit.PinEditScreen
import com.issueissyu.fe.ui.screens.mypage.AlarmSettingScreen
import com.issueissyu.fe.ui.screens.mypage.MyIssueScreen
import com.issueissyu.fe.ui.screens.mypage.MyPageEvent
import com.issueissyu.fe.ui.screens.mypage.MyPageScreen
import com.issueissyu.fe.ui.screens.mypage.MyPageTermScreen
import com.issueissyu.fe.ui.screens.mypage.ProfileChangeScreen
import com.issueissyu.fe.ui.screens.collection.CollectionScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current // LocalContext 추가

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

        composable(AppDestinations.Onboarding.LANDING_ROUTE) {
            // 랜딩 페이지가 아직 없으므로 로그인 화면으로 대체하거나 빈 화면 표시
            LoginScreen(
                viewModel = hiltViewModel(),
                onNavigateToMain = {
                    navController.navigate(AppDestinations.HOME_ROUTE) {
                        popUpTo(AppDestinations.Onboarding.LANDING_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(AppDestinations.Onboarding.SIGNUP_ROUTE)
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
        composable(AppDestinations.COLLECTION_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CollectionScreen()
            }
        }
        composable(AppDestinations.TOWN_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MapScreen(navController = navController)
            }
        }
        composable(AppDestinations.COMMUNITY_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("커뮤니티 화면 (준비 중)")
            }
        }
        composable(AppDestinations.MyPage.MYPAGE_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MyPageScreen(
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
                                navController.navigate(AppDestinations.Onboarding.LANDING_ROUTE)
                            }
                            MyPageEvent.NavigateToTerm -> {
                                navController.navigate(AppDestinations.MyPage.TERMS_ROUTE)
                            }
                            MyPageEvent.Logout -> {
                                // TODO: 로그아웃 처리
                            }
                            MyPageEvent.Withdraw -> {
                                // TODO: 회원탈퇴 처리
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        composable(AppDestinations.MyPage.PROFILE_CHANGE_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ProfileChangeScreen(
                    onBackClick = { navController.popBackStack() },
                    onCollectionClick = {
                        navController.navigate(AppDestinations.COLLECTION_ROUTE)
                    },
                    onCompleteClick = { navController.popBackStack() }
                )
            }
        }
        composable(AppDestinations.MyPage.LOCAL_CHANGE_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LocalVerificationScreen(
                    onBackClick = { navController.popBackStack() },
                    onCompleteRegisterClick = { navController.popBackStack() }
                )
            }
        }
        composable(AppDestinations.MyPage.TERMS_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MyPageTermScreen(
                    onBackClick = { navController.popBackStack() },
                    onTermsDetailClick = { termsType ->
                        // MyPageTermScreen의 TermsType은 LocalVerificationScreen 등과 다를 수 있으므로 확인 필요
                        // 여기서는 단순 popBackStack 또는 상세 이동 처리
                    }
                )
            }
        }
        composable(AppDestinations.MyPage.MY_ISSUES_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MyIssueScreen(
                    onBackClick = { navController.popBackStack() },
                    onPinClick = { pinId, _, _ ->
                        navController.navigate(AppDestinations.pinDetailRoute(pinId))
                    }
                )
            }
        }
        composable(AppDestinations.MyPage.ALARM_SETTINGS_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AlarmSettingScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
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
                    },
                    onEditClick = { editPinId ->
                        navController.navigate(AppDestinations.pinEditRoute(editPinId))
                    },
                    onDeleted = { navController.popBackStack() }
                )
            }
        }
        composable(
            route = AppDestinations.PIN_EDIT_ROUTE,
            arguments = listOf(
                navArgument("pinId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val editPinId = backStackEntry.arguments?.getString("pinId").orEmpty()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                PinEditScreen(
                    pinId = editPinId,
                    onBackClick = { navController.popBackStack() },
                    onEdited = { editedPinId ->
                        navController.navigate(AppDestinations.pinDetailRoute(editedPinId)) {
                            popUpTo(AppDestinations.PIN_DETAIL_ROUTE) {
                                inclusive = true
                            }
                        }
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
            val context = LocalContext.current // LocalContext 선언

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
                        // TODO: 실제 API 연결 후 성공 시 피드백 표시 및 화면 복귀 처리
                        Toast.makeText(context, "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show()
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
                        onCreated = { createdPinId ->
                            // TODO: 실제 생성 API 연결 후 지도 목록 새로고침까지 함께 처리.
                            navController.popBackStack()
                            navController.navigate(AppDestinations.pinDetailRoute(createdPinId))
                        }
                    )
                }
            }
        }
    }
}
