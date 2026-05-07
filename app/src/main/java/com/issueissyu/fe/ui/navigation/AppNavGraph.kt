package com.issueissyu.fe.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.issueissyu.fe.ui.screens.home.HomeScreen
//import com.issueissyu.fe.ui.screens.map.MapScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.issueissyu.fe.ui.navigation.AppDestinations.Onboarding.LOGIN_ROUTE
import com.issueissyu.fe.ui.screens.onboarding.CompleteScreen
import com.issueissyu.fe.ui.screens.onboarding.TermScreen
import com.issueissyu.fe.ui.screens.onboarding.TermsType
import com.issueissyu.fe.ui.screens.onboarding.LoginScreen
import com.issueissyu.fe.ui.screens.onboarding.SignUpScreen
import com.issueissyu.fe.ui.screens.onboarding.SplashScreen
import com.issueissyu.fe.ui.screens.onboarding.TermDetailScreen
import com.issueissyu.fe.ui.screens.onboarding.UserVerificationScreen
import com.issueissyu.fe.ui.screens.onboarding.LocalVerificationScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.Onboarding.SPLASH_ROUTE
    ) {
        //온보딩
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

        //회원 가입
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

        //온보딩 시작
        composable(AppDestinations.Onboarding.TERM_ROUTE){
            BackHandler {
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(0) { inclusive = true }
                }
            }

            TermScreen(
                onAgreeClick = { navController.navigate(AppDestinations.Onboarding.USER_VERIFICATION_ROUTE) },
                onTermsDetailClick = {  termsType ->
                    navController.navigate(
                        AppDestinations.Onboarding.termDetailRoute(termsType.name)
                    )
                }
            )
        }

        composable(
            route=AppDestinations.Onboarding.TERM_DETAIL_ROUTE,
            arguments = listOf(
                navArgument("termsType"){ type = NavType.StringType}
            )
        ){  backStackEntry ->
            val termsType = backStackEntry.arguments?.getString("termsType")?.let {
                TermsType.valueOf(it)
            } ?: TermsType.SERVICE

            TermDetailScreen(
                termsType = termsType,
                onBackClick = { navController.navigateUp()}
            )

        }

        composable(AppDestinations.Onboarding.USER_VERIFICATION_ROUTE){
            BackHandler {
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(0) { inclusive = true }
                }
            }

            UserVerificationScreen(
                onVerificationComplete = { nickname, email, phoneNumber ->
                    navController.navigate(AppDestinations.Onboarding.LOCAL_VERIFICATION_ROUTE)
                }
            )
        }

        composable(AppDestinations.Onboarding.LOCAL_VERIFICATION_ROUTE){
            BackHandler {
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(0) { inclusive = true }
                }
            }

            LocalVerificationScreen(
                onCompleteRegisterClick = { navController.navigate(AppDestinations.Onboarding.COMPLETE_ROUTE)}
            )
        }

        //완료 - 메인화면 이동 시 전체 스택 제거
        composable(AppDestinations.Onboarding.COMPLETE_ROUTE){
            BackHandler {
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(0) { inclusive = true }
                }
            }

            CompleteScreen(
                onNavigateToMain = { navController.navigate(AppDestinations.HOME_ROUTE){
                    popUpTo(0) { inclusive = true }
                } },
                onNavigateToLanding = {}    //추후 랜딩 페이지 연결
            )

        }

        composable(AppDestinations.HOME_ROUTE) {
            /*Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                HomeScreen() // modifier 파라미터 제거
            }*/
        }
        composable(AppDestinations.COLLECTION_ROUTE) { /* TODO: CollectionScreen */ }
        composable(AppDestinations.TOWN_ROUTE) {
            /*Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MapScreen(navController = navController) // modifier 파라미터 제거
            }*/
        }
        composable(AppDestinations.COMMUNITY_ROUTE) { /* TODO: CommunityScreen */ }
        composable(AppDestinations.MYPAGE_ROUTE) { /* TODO: MypageScreen */ }
        /*composable(AppDestinations.PATCH_NOTE_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("패치노트 화면")
            }
        }
        composable(AppDestinations.PIN_CREATION_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("핀 생성 화면")
            }
        }*/
    }
}
