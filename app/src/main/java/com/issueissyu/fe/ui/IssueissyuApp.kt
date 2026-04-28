package com.issueissyu.fe.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.issueissyu.fe.ui.navigation.AppNavGraph
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.issueissyu.fe.ui.components.BottomNavigationBar // BottomNavigationBar 다시 활성화
import androidx.compose.ui.Modifier
import com.issueissyu.fe.ui.navigation.AppDestinations // AppDestinations import 추가

@Composable
fun App() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val effectiveRoute = currentRoute ?: AppDestinations.TOWN_ROUTE // fallback 로직 추가

    val bottomBarRoutes = setOf( // 하단바 표시 라우트 정의
        AppDestinations.COLLECTION_ROUTE,
        AppDestinations.TOWN_ROUTE,
        AppDestinations.COMMUNITY_ROUTE,
        AppDestinations.MYPAGE_ROUTE
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (effectiveRoute in bottomBarRoutes) { // 조건부 표시
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = effectiveRoute
                )
            }
        }
    ) { paddingValues ->
        AppNavGraph(navController = navController, paddingValues = paddingValues)
    }
}