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
import com.issueissyu.fe.ui.screens.patchnote.PatchNotesRoute
import com.issueissyu.fe.ui.screens.pindetail.PinDetailScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.Onboarding.SPLASH_ROUTE
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
                    navController.navigate(AppDestinations.Onboarding.LOGIN_ROUTE) {
                        popUpTo(AppDestinations.Onboarding.SPLASH_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.Onboarding.LOGIN_ROUTE) {
            LoginScreen(
                viewModel = hiltViewModel(),
                onNavigateToMain = {
                    navController.navigate(AppDestinations.HOME_ROUTE) {
                        popUpTo(AppDestinations.Onboarding.LOGIN_ROUTE) { inclusive = true }
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
                    onPatchNoteClick = { pinId ->
                        // TODO: 핀 상세 화면 route 확정 후 이동
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
                    onBackClick = { navController.popBackStack() }
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
            val pinLat = backStackEntry.arguments?.getFloat("pinLat") ?: 0f
            val pinLng = backStackEntry.arguments?.getFloat("pinLng") ?: 0f
            val userLat = backStackEntry.arguments?.getFloat("userLat") ?: 0f
            val userLng = backStackEntry.arguments?.getFloat("userLng") ?: 0f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("핀 생성 화면: $pinType, PinLat: $pinLat, PinLng: $pinLng, UserLat: $userLat, UserLng: $userLng")
            }
        }
    }
}
