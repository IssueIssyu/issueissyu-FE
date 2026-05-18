package com.issueissyu.fe.ui

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.issueissyu.fe.ui.components.BottomNavigationBar
import com.issueissyu.fe.ui.navigation.AppDestinations
import com.issueissyu.fe.ui.navigation.AppNavGraph

private fun shouldShowBottomBar(route: String?): Boolean {
    if (route == null) return false
    return route == AppDestinations.COLLECTION_ROUTE ||
        route == AppDestinations.TOWN_ROUTE ||
        route == AppDestinations.COMMUNITY_ROUTE ||
        route == AppDestinations.MYPAGE_ROUTE ||
        route == AppDestinations.PATCH_NOTE_ROUTE ||
        route.startsWith("${AppDestinations.PIN_CREATION_ROUTE}?") ||
        route == AppDestinations.PIN_CREATION_ROUTE
}

@Composable
fun App() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute
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
