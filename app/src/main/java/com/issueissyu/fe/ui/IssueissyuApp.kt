package com.issueissyu.fe.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.issueissyu.fe.ui.navigation.AppNavGraph

@Composable
fun App() {
    val navController = rememberNavController()
    AppNavGraph(navController = navController)
}
