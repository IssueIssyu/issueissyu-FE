package com.issueissyu.fe.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.issueissyu.fe.R
import androidx.compose.ui.text.font.FontWeight
import com.issueissyu.fe.ui.theme.suiteFontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.issueissyu.fe.ui.navigation.AppDestinations
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.IssueissyuTheme

data class BottomNavItem(
    val label: String,
    val icon: Int,
    val selectedIcon: Int,
    val route: String
)

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val items = listOf(
        BottomNavItem(
            label = "컬렉션",
            icon = R.drawable.collectiondefault,
            selectedIcon = R.drawable.collectionselected,
            route = AppDestinations.COLLECTION_ROUTE
        ),
        BottomNavItem(
            label = "동네",
            icon = R.drawable.towndefault,
            selectedIcon = R.drawable.townselected,
            route = AppDestinations.TOWN_ROUTE
        ),
        BottomNavItem(
            label = "커뮤니티",
            icon = R.drawable.communitydefault,
            selectedIcon = R.drawable.communityselected,
            route = AppDestinations.COMMUNITY_ROUTE
        ),
        BottomNavItem(
            label = "마이페이지",
            icon = R.drawable.mypagedefault,
            selectedIcon = R.drawable.mypageselected,
            route = AppDestinations.MYPAGE_ROUTE
        )
    )

    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = if (selected) item.selectedIcon else item.icon),
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontFamily = suiteFontFamily,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandColor,
                    unselectedIconColor = Gray_5,
                    selectedTextColor = BrandColor,
                    unselectedTextColor = Gray_5,
                    indicatorColor = Color.Transparent
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    IssueissyuTheme {
        val navController = rememberNavController()
        // 현재 라우트를 가정합니다 (예: Collection이 선택된 상태)
        val currentRoute = AppDestinations.COLLECTION_ROUTE

        BottomNavigationBar(navController = navController, currentRoute = currentRoute)
    }
}