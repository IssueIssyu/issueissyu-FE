package com.issueissyu.fe.ui.screens.map

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.components.CategoryButtons
import com.issueissyu.fe.ui.components.CategoryItem
import com.issueissyu.fe.ui.navigation.AppDestinations
import com.issueissyu.fe.ui.screens.map.AutoScrollingNotice
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Shop
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.White
import com.issueissyu.fe.ui.theme.IssueTypo
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap

@Composable
fun MapScreen(
    navController: NavHostController,
    // viewModel: MapViewModel = hiltViewModel() // ViewModel이 필요하다면
) {
    var showResearchButton by remember { mutableStateOf(false) } // 재탐색 버튼 상태
    var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) } // NaverMap 인스턴스

    // 2.3. 지도 핀 관련 상태 구조 정의 (기반 마련)
    data class MapPin(
        val id: String,
        val position: LatLng,
        val title: String,
        val type: String // 예: "Issue", "Shop"
    )

    var pins by remember { mutableStateOf(emptyList<MapPin>()) } // 지도 핀 목록
    var selectedPin by remember { mutableStateOf<MapPin?>(null) } // 선택된 핀
    var visibleBounds by remember { mutableStateOf<LatLngBounds?>(null) } // 현재 지도 화면에 보이는 영역

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. 네이버 지도 (전체 화면 배경)
        NaverMapComposable(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                naverMapInstance = map
                // TODO: 앱 실행 시 지도 카메라를 현재 위치로 이동하는 로직 추가 (위치 권한 처리 후)
                // 예: map.locationTrackingMode = LocationTrackingMode.Follow
            }
        )

        // 상단 UI 영역 (카테고리 버튼, 공지)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 16.dp) // 상단 여백
        ) {
            // 상단 카테고리 버튼 (알림 기능 활성화)
            val errorContainer = MaterialTheme.colorScheme.errorContainer
            val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
            val primaryContainer = MaterialTheme.colorScheme.primaryContainer
            val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
            val sampleCategories = remember {
                listOf(
                    CategoryItem("이슈", R.drawable.issue, Issue, errorContainer),
                    CategoryItem("소통", R.drawable.communicate, Communication, secondaryContainer),
                    CategoryItem("가게", R.drawable.shop, Shop, primaryContainer),
                    CategoryItem("축제", R.drawable.festival, Festival, tertiaryContainer)
                )
            }
            var selectedCategory by remember { mutableStateOf<String?>(null) } // 임시 선택 상태

            CategoryButtons(
                categories = sampleCategories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                onNotificationClick = { /* 알림 버튼 클릭 시 동작 */ },
                modifier = Modifier.fillMaxWidth()
            )
            // 공지 부분
            val mockNotices = remember {
                listOf(
                    "오늘의 공지: 새로운 업데이트가 있습니다!",
                    "두 번째 공지: 버그 수정 및 성능 개선",
                    "세 번째 공지: 새로운 이벤트가 시작됩니다!"
                )
            }
            AutoScrollingNotice(
                notices = mockNotices,
                iconResId = R.drawable.ic_megaphone,
                onClick = { clickedNotice -> /* TODO: 클릭된 공지 내용으로 이동 또는 상세 보기 */ },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // 2.1. 이 지역 내 재탐색 버튼 UI 및 showResearchButton 상태 구현
        if (showResearchButton) {
            Button(
                onClick = { showResearchButton = false /* TODO: 실제 핀 데이터 재조회 로직 연결 */ },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 192.dp)
                    .size(width = 153.dp, height = 31.dp),
                shape = RoundedCornerShape(15.5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gray_7),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "재탐색",
                    tint = White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "이 지역 내 재탐색",
                    style = IssueTypo.Bold12.copy(color = White)
                )
            }
        }

        // 우측 하단 버튼들 (내 위치, 패치노트, 핀 생성) - 순서 중요 (아래부터 위로 쌓임)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp), // 하단바 높이 패딩은 AppNavGraph에서 처리됨
            horizontalAlignment = Alignment.End
        ) {
            // 패치노트 버튼 (가장 위)
            Icon(
                painter = painterResource(id = R.drawable.patchnotebutton),
                contentDescription = "패치노트",
                modifier = Modifier
                    .size(56.dp)
                    .clickable { navController.navigate(AppDestinations.PATCH_NOTE_ROUTE) },
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.height(8.dp)) // 버튼 간 간격
            // 내 위치 찾기 버튼 (중간)
            Icon(
                painter = painterResource(id = R.drawable.findspot),
                contentDescription = "내 위치 찾기",
                modifier = Modifier
                    .size(48.dp) // 작은 크기
                    .clickable {
                        // TODO: 위치 권한 확인 및 현재 위치 가져오기 로직
                        // TODO: naverMapInstance?.moveCamera(CameraUpdate.scrollTo(LatLng(latitude, longitude)))
                    },
                tint = Color.Unspecified // 아이콘 색상 (theme에서 가져옴)
            )
            Spacer(modifier = Modifier.height(8.dp)) // 버튼 간 간격
            // 핀 생성 버튼 (가장 아래)
            Icon(
                painter = painterResource(id = R.drawable.pinbutton),
                contentDescription = "핀 생성",
                modifier = Modifier
                    .size(56.dp)
                    .clickable { navController.navigate(AppDestinations.PIN_CREATION_ROUTE) },
                tint = Color.Unspecified
            )
        }

        // 2.4. 핀 클릭 시 하단 카드 UI를 조건부로 표시할 수 있는 selectedPin 상태 구조 준비
        if (selectedPin != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(150.dp) // 임시 높이
                    .background(MaterialTheme.colorScheme.surface) // 임시 배경색
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "핀 요약 카드 (TODO: PinSummaryCard 구현)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// MapView 로직을 별도로 분리한 컴포저블
@Composable
fun NaverMapComposable(
    modifier: Modifier = Modifier,
    onMapReady: (NaverMap) -> Unit
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier.background(Color.LightGray), // 프리뷰 배경색
            contentAlignment = Alignment.Center
        ) {
            Text("지도 프리뷰 (현재 위치 설정)", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
        }
    } else {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val mapView = remember { MapView(context) }

        DisposableEffect(lifecycleOwner) {
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                    else -> {}
                }
            }

            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
                // 필요하다면 mapView 관련 리소스 해제
            }
        }

        AndroidView(
            modifier = modifier,
            factory = {
                mapView.getMapAsync { map ->
                    onMapReady(map)
                }
                mapView
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMapScreen() {
    IssueissyuTheme {
        MapScreen(navController = rememberNavController())
    }
}