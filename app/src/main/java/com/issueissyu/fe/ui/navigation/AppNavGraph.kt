package com.issueissyu.fe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.issueissyu.fe.ui.screens.home.HomeScreen
import com.issueissyu.fe.ui.screens.map.MapScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun AppNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.TOWN_ROUTE
    ) {
        composable(AppDestinations.HOME_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                HomeScreen() // modifier 파라미터 제거
            }
        }
        composable(AppDestinations.COLLECTION_ROUTE) { /* TODO: CollectionScreen */ }
        composable(AppDestinations.TOWN_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MapScreen(navController = navController) // modifier 파라미터 제거
            }
        }
        composable(AppDestinations.COMMUNITY_ROUTE) { /* TODO: CommunityScreen */ }
        composable(AppDestinations.MYPAGE_ROUTE) { /* TODO: MypageScreen */ }
        composable(AppDestinations.PATCH_NOTE_ROUTE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("패치노트 화면")
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
                    nullable = false
                },
                navArgument("pinLng") {
                    type = NavType.FloatType
                    defaultValue = 0f
                    nullable = false
                },
                navArgument("userLat") {
                    type = NavType.FloatType
                    defaultValue = 0f
                    nullable = false
                },
                navArgument("userLng") {
                    type = NavType.FloatType
                    defaultValue = 0f
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val pinType = backStackEntry.arguments!!.getString("type")
            val pinLat = backStackEntry.arguments!!.getFloat("pinLat")
            val pinLng = backStackEntry.arguments!!.getFloat("pinLng")
            val userLat = backStackEntry.arguments!!.getFloat("userLat")
            val userLng = backStackEntry.arguments!!.getFloat("userLng")

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