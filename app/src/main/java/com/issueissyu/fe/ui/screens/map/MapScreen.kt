// MapScreen.kt 파일은 지도 화면의 UI와 관련 로직을 정의합니다.
// 네이버 지도 SDK를 사용하여 지도 표시, 핀 관리, 사용자 현재 위치 표시 등의 기능을 구현합니다.

package com.issueissyu.fe.ui.screens.map

// ==============================================================================================
// 1. Android 권한 및 Jetpack Compose 관련 Import
//    - 위치 권한 처리를 위한 Android Manifest, Activity, Context, PackageManager 관련 클래스 임포트
//    - Jetpack Compose UI 및 상태 관리를 위한 다양한 컴포넌트 임포트
//    - 네이버 지도 SDK의 LocationTrackingMode, FusedLocationSource 등 지도 관련 클래스 임포트
// ==============================================================================================

import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.util.FusedLocationSource
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.ui.screens.map.MapViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
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
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.CommunicationContainerLight
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.FestivalContainer
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueContainer
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Shop
import com.issueissyu.fe.ui.theme.ShopContainer
import com.issueissyu.fe.ui.theme.Text
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap

// 위치 권한 요청 코드 상수
private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

// Context에서 Activity를 찾는 헬퍼 함수
private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

// ==============================================================================================
// 2. Enum 및 Data Class 정의
//    - PinCategory: 지도에 표시될 핀의 카테고리 (이슈, 소통, 가게, 축제)를 정의
//    - MapPin: 지도 핀의 데이터 구조 (ID, 위치, 제목, 카테고리, 설명 등)를 정의
// ==============================================================================================

// TODO: 핀 담당자가 공용 Pin 모델을 완성하면 MapPin 및 PinCategory/PinType 필드와 연결하기
// 지도 페이지에서는 category 기준으로 핀 색상/모양을 분기해야 함
// 패치노트 페이지에서는 ISSUE 카테고리 핀만 전달받도록 연결하기
// ISSUE 핀은 resolutionStatus 값을 가져야 하며, 상태별 색상 분기가 필요함

private enum class ResolutionStatus { // 패치노트 페이지 요구사항에 따라 추가
    BEFORE_RESOLUTION,
    IN_PROGRESS,
    RESOLVED
}

private enum class PinCategory {
    ISSUE,
    COMMUNICATION,
    SHOP,
    FESTIVAL
}

private data class MapPin(
    val id: String,
    val position: LatLng,
    val title: String,
    val category: PinCategory,
    val description: String,
    val locationName: String,
    val imageUrl: String? = null
    // TODO: 핀 담당자가 resolutionStatus 필드를 IssuePin에 포함하면 여기에 반영
    // val resolutionStatus: ResolutionStatus? = null // 현재 MapPin에 직접 추가하지 않고, TODO로 남김
)

// ==============================================================================================
// 3. MapScreen Composable 함수
//    - 지도 화면 전체의 UI 구성을 담당하는 메인 Composable
//    - 지도, 상단 카테고리 버튼, 공지사항, 하단 버튼 (패치노트, 현재 위치, 핀 생성), 핀 카드 등의 UI 요소를 포함
//    - 지도 인스턴스, 핀 목록, 선택된 핀, 현재 지도 범위 등의 상태를 관리
// ==============================================================================================
@Composable
fun MapScreen(
    navController: NavHostController,
    viewModel: MapViewModel = hiltViewModel() // ViewModel 주입
) {
    // ViewModel의 상태를 관찰합니다.
    val showResearchButton by viewModel.showResearchButton.collectAsStateWithLifecycle()
    val showPinTypeSelector by viewModel.showPinTypeSelector.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    // 네이버 지도 인스턴스 (NaverMap 객체) 상태
    var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) }

    // TODO: 실제 핀/마커 구현 시 API 또는 ViewModel 상태와 연결
    // 지도에 표시될 핀 목록 상태
    var pins by remember { mutableStateOf(emptyList<MapPin>()) }
    // 현재 선택된 핀의 정보 상태 (MapPin은 private data class이므로 MapScreen에 유지)
    var selectedPin by remember { mutableStateOf<MapPin?>(null) }
    // 현재 지도의 가시 영역(LatLngBounds) 상태
    var visibleBounds by remember { mutableStateOf<LatLngBounds?>(null) }

    // Context와 Activity 가져오기
    val context = LocalContext.current
    val activity = context.findActivity()

    // FusedLocationSource 준비
    val locationSource = remember(activity) {
        activity?.let {
            FusedLocationSource(it, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    // 위치 권한 확인 함수
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    // 권한 요청 런처
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            naverMapInstance?.locationTrackingMode = LocationTrackingMode.Follow
            naverMapInstance?.locationOverlay?.isVisible = true
        } else {
            naverMapInstance?.locationTrackingMode = LocationTrackingMode.None
            // TODO: 권한 거부 안내 UI 또는 Toast 처리
        }
    }

    // 현재 위치로 이동 함수
    fun moveToCurrentLocation() {
        val map = naverMapInstance ?: return

        if (hasLocationPermission()) {
            map.locationTrackingMode = LocationTrackingMode.Follow
            map.locationOverlay.isVisible = true
            // TODO: FusedLocationSource를 통해 받은 현재 위치로 카메라 이동
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // 앱 실행 시 한 번 현재 위치 이동 (권한이 이미 허용되어 있으면)
    var hasRequestedInitialLocation by remember { mutableStateOf(false) }

    LaunchedEffect(naverMapInstance) {
        if (naverMapInstance != null && !hasRequestedInitialLocation) {
            hasRequestedInitialLocation = true
            moveToCurrentLocation()
        }
    }


    // TODO: UI 확인이 필요하면 임시로 selectedPin에 mock 데이터를 넣고 확인
    // 현재 선택된 핀에 대한 Mock 데이터 예시 (개발 및 테스트용)
    // selectedPin = MapPin(
    //     id = "1",
    //     title = "침수된 도로",
    //     category = PinCategory.ISSUE,
    //     description = "어제 비로 도로 일부가 침수되어 통행이 어렵습니다. 우회해야 할 것 같습니다.",
    //     locationName = "역삼동 테헤란로 123",
    //     position = LatLng(0.0, 0.0)
    // )

    // 화면 전체를 채우는 Box 레이아웃. 지도 및 다른 UI 요소들을 그 위에 쌓음
    Box(modifier = Modifier.fillMaxSize()) {
        // NaverMapComposable: 네이버 지도 뷰를 표시하는 Composable
        NaverMapComposable(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                // 지도가 준비되면 NaverMap 인스턴스를 상태에 저장
                naverMapInstance = map

                // LocationSource 연결
                locationSource?.let {
                    map.locationSource = it
                }

                if (hasLocationPermission()) {
                    map.locationOverlay.isVisible = true
                    map.locationTrackingMode = LocationTrackingMode.Follow
                } else {
                    map.locationOverlay.isVisible = false
                    map.locationTrackingMode = LocationTrackingMode.None
                }


                // TODO: 지도 이동/줌 변경 감지 후 showResearchButton = true 처리
                // 현재는 모든 카메라 변경 시 재탐색 버튼이 뜨지만, 나중에는 사용자 제스처 이동일 때만 띄우도록 개선해야 합니다.
                // 지도 이동 또는 줌 레벨 변경 시 '이 지역 내 재탐색' 버튼을 표시하는 로직이 들어갈 예정
                map.addOnCameraChangeListener { _, _ ->
                    viewModel.showResearchAreaButton()
                }


                // TODO: 현재 지도 bounds를 visibleBounds에 저장
                // 현재 지도의 가시 영역을 visibleBounds 상태에 저장하는 로직이 들어갈 예정
            }
        )

        // 상단 UI 영역: 카테고리 버튼과 자동 스크롤 공지사항
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            val errorContainer = MaterialTheme.colorScheme.errorContainer
            val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
            val primaryContainer = MaterialTheme.colorScheme.primaryContainer
            val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer

            // 샘플 카테고리 데이터 정의 (Issue, Communication, Shop, Festival)
            val sampleCategories = remember(
                errorContainer,
                secondaryContainer,
                primaryContainer,
                tertiaryContainer
            ) {
                listOf(
                    CategoryItem("이슈", R.drawable.issue, Issue, errorContainer),
                    CategoryItem("소통", R.drawable.communicate, Communication, secondaryContainer),
                    CategoryItem("가게", R.drawable.shop, Shop, primaryContainer),
                    CategoryItem("축제", R.drawable.festival, Festival, tertiaryContainer)
                )
            }

            // CategoryButtons Composable: 카테고리 선택 버튼들을 표시
            CategoryButtons(
                categories = sampleCategories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    viewModel.onCategorySelected(category)
                    // TODO: 실제 핀 구현 후 선택 카테고리에 맞는 핀만 필터링
                    // 카테고리 선택 시 해당 카테고리에 맞는 핀만 지도에 표시하는 로직이 들어갈 예정
                },
                onNotificationClick = {
                    // TODO: 알림 목록 UI 또는 알림 화면 연결
                    // 알림 아이콘 클릭 시 알림 목록 화면으로 이동하는 로직이 들어갈 예정
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Mock 공지사항 데이터 (개발 및 테스트용)
            val mockNotices = remember {
                listOf(
                    "오늘의 공지: 새로운 업데이트가 있습니다!",
                    "두 번째 공지: 버그 수정 및 성능 개선",
                    "세 번째 공지: 새로운 이벤트가 시작됩니다!"
                )
            }

            // AutoScrollingNotice Composable: 자동 스크롤되는 공지사항 배너
            AutoScrollingNotice(
                notices = mockNotices,
                iconResId = R.drawable.ic_megaphone,
                onClick = { clickedNotice ->
                    // TODO: 클릭된 공지 상세 보기 또는 이동
                    // 공지사항 클릭 시 상세 내용 보기 또는 관련 화면으로 이동하는 로직이 들어갈 예정
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // '이 지역 내 재탐색' 버튼: 지도를 움직인 후 현재 보이는 영역의 핀을 다시 불러올 때 사용
        if (showResearchButton) {
            Button(
                onClick = {
                    viewModel.hideResearchAreaButton() // ViewModel의 함수 호출
                    // TODO: visibleBounds 기준으로 핀 목록 재조회
                    // 버튼 클릭 시 현재 지도의 visibleBounds를 기준으로 핀 목록을 다시 조회하는 로직이 들어갈 예정
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 192.dp)
                    .size(width = 153.dp, height = 31.dp),
                shape = RoundedCornerShape(15.5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gray_7,
                    contentColor = White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "재탐색",
                    tint = White,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "이 지역 내 재탐색",
                    style = IssueTypo.Bold12.copy(color = White)
                )
            }
        }

        // 하단 우측 버튼 영역: 패치노트, 현재 위치, 핀 생성 버튼
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 패치노트 버튼
            Icon(
                painter = painterResource(id = R.drawable.patchnotebutton),
                contentDescription = "패치노트",
                modifier = Modifier
                    .size(56.dp)
                    .clickable {
                        navController.navigate(AppDestinations.PATCH_NOTE_ROUTE) // 패치노트 화면으로 이동
                    },
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 현재 위치 찾기 버튼
            Icon(
                painter = painterResource(id = R.drawable.findspot),
                contentDescription = "내 위치 찾기",
                modifier = Modifier
                    .size(48.dp)
                    .clickable {
                        moveToCurrentLocation() // ViewModel의 함수 호출 (이전 TODO 대신 실제 함수 호출)
                    },
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 핀 생성 버튼 (Floating Action Button)
            FloatingActionButton(
                onClick = { viewModel.openPinTypeSelector() }, // ViewModel의 함수 호출
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                containerColor = BrandColor
            ) {
                Icon(
                    imageVector = Icons.Default.AddLocation,
                    contentDescription = "핀 생성",
                    tint = White
                )
            }
        }

        // 핀 카드 외부 클릭 시 닫기
        // 지도/상단 UI/우측 버튼을 모두 그린 뒤, 카드 바로 앞에 배치해야 함
        if (selectedPin != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { selectedPin = null }
            )
        }

        selectedPin?.let { pin ->
            PinSummaryCard(
                pin = pin,
                onDismiss = { selectedPin = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 12.dp
                        )
            )
        }

        // 핀 타입 선택 UI 외부 클릭 시 닫기 레이어
        if (showPinTypeSelector) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { viewModel.closePinTypeSelector() } // ViewModel의 함수 호출
            )
        }

        if (showPinTypeSelector) {
            PinTypeSelector(
                onIssueClick = {
                    viewModel.closePinTypeSelector()
                    navController.navigate("${AppDestinations.PIN_CREATION_ROUTE}?type=issue") // 이슈 핀 생성 화면으로 이동
                },
                onCommunicationClick = {
                    viewModel.closePinTypeSelector()
                    navController.navigate("${AppDestinations.PIN_CREATION_ROUTE}?type=communication") // 소통 핀 생성 화면으로 이동
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 16.dp + 64.dp)
            )
        }
    }
}

// ==============================================================================================
// 4. PinSummaryCard Composable 함수
//    - 지도에서 특정 핀을 선택했을 때 하단에 표시되는 핀 상세 요약 카드 UI
//    - 핀의 카테고리에 따라 배경색이 달라짐
//    - 핀의 제목, 위치 이름, 설명, 이미지 플레이스홀더, 상세 보기 버튼을 포함
// ==============================================================================================
@Composable
private fun PinSummaryCard(
    pin: MapPin,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: 핀 담당자가 공용 Pin 모델을 완성하면 category 또는 PinType 기준으로 색상 분기 로직 업데이트
    val cardBackgroundColor = when (pin.category) {
        PinCategory.ISSUE -> IssueContainer
        PinCategory.COMMUNICATION -> CommunicationContainerLight
        PinCategory.SHOP -> ShopContainer
        PinCategory.FESTIVAL -> FestivalContainer
    }

    Box(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(30.dp))
            .clip(RoundedCornerShape(30.dp))
            .background(cardBackgroundColor)
            .clickable {
                // 카드 내부 클릭 시 외부 dismiss layer로 이벤트가 전달되지 않도록 소비
                // 이 부분을 클릭해도 selectedPin = null 이 되지 않도록 이벤트 소비
            }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pin.title,
                        style = IssueTypo.Bold18.copy(color = Title)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = pin.locationName,
                        style = IssueTypo.Regular12.copy(color = Gray_7)
                    )
                }

                PinImagePlaceholder(
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pin.description,
                style = IssueTypo.Regular15.copy(color = Text)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "상세 보기",
                style = IssueTypo.Bold12.copy(color = BrandColor),
                modifier = Modifier.clickable {
                    // TODO: 핀 상세 화면 이동
                    // 상세 보기 클릭 시 해당 핀의 상세 화면으로 이동하는 로직이 들어갈 예정
                }
            )
        }
    }
}

// ==============================================================================================
// 5. PinImagePlaceholder Composable 함수
//    - 핀 카드에 이미지가 없을 경우 표시되는 이미지 플레이스홀더 UI
// ==============================================================================================
@Composable
private fun PinImagePlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Gray_3),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "이미지",
            style = IssueTypo.Regular12.copy(color = Text)
        )
    }
}

// ==============================================================================================
// 6. PinTypeSelector Composable 함수
//    - 핀 생성 버튼 클릭 시 나타나는, 생성할 핀의 타입을 선택하는 UI
//    - '이슈 생성'과 '소통 생성' 버튼을 포함
// ==============================================================================================
@Composable
private fun PinTypeSelector(
    onIssueClick: () -> Unit,
    onCommunicationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(82.dp)
            .height(172.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(32.dp)
            )
            .background(
                color = White,
                shape = RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 7.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // '이슈 생성' 버튼 아이템
        PinTypeSelectorItem(
            text = "이슈 생성",
            iconResId = R.drawable.issue,
            backgroundColor = IssueContainer,
            onClick = onIssueClick
        )

        // '소통 생성' 버튼 아이템
        PinTypeSelectorItem(
            text = "소통 생성",
            iconResId = R.drawable.communicate,
            backgroundColor = CommunicationContainerLight,
            onClick = onCommunicationClick
        )
    }
}

// ==============================================================================================
// 7. PinTypeSelectorItem Composable 함수
//    - PinTypeSelector 내부에 사용되는 개별 핀 타입 선택 버튼 UI (아이콘과 텍스트)
// ==============================================================================================
@Composable
private fun PinTypeSelectorItem(
    text: String,
    iconResId: Int,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = text,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = text,
            style = IssueTypo.Bold12.copy(color = Title)
        )
    }
}

// ==============================================================================================
// 8. NaverMapComposable 함수
//    - 네이버 지도 SDK의 MapView를 Jetpack Compose에 통합하는 Composable
//    - AndroidView를 사용하여 기존 Android View를 Compose에 렌더링
//    - LifecycleEventObserver를 통해 MapView의 생명주기를 Compose 생명주기에 동기화
//    - `onMapReady` 콜백을 통해 `NaverMap` 객체가 준비되면 외부로 전달
// ==============================================================================================
@Composable
fun NaverMapComposable(
    modifier: Modifier = Modifier,
    onMapReady: (NaverMap) -> Unit
) {
    // LocalInspectionMode: Compose 프리뷰 모드에서 실행 중인지 확인
    if (LocalInspectionMode.current) {
        // 프리뷰 모드일 경우 지도를 실제 로드하지 않고 대체 UI 표시
        Box(
            modifier = modifier.background(Gray_3),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "지도 프리뷰 (현재 위치 설정)",
                style = MaterialTheme.typography.bodyLarge.copy(color = Text)
            )
        }
    } else {
        // 실제 앱 실행 모드일 경우 네이버 지도 MapView 렌더링
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val mapView = remember { MapView(context) } // MapView 인스턴스 생성 및 기억

        // DisposableEffect: Composable의 생명주기에 따라 MapView의 생명주기 메서드 호출
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

            lifecycleOwner.lifecycle.addObserver(lifecycleObserver) // 생명주기 옵저버 추가

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver) // Composable 제거 시 옵저버 제거
            }
        }

        // AndroidView: Android View (MapView)를 Compose UI에 통합
        AndroidView(
            modifier = modifier,
            factory = {
                mapView.getMapAsync { map ->
                    onMapReady(map) // 지도가 준비되면 NaverMap 객체를 콜백으로 전달
                }
                mapView // 생성된 MapView 반환
            }
        )
    }
}

// ==============================================================================================
// 9. Preview 함수들
//    - Jetpack Compose의 @Preview 어노테이션을 사용하여 UI 컴포넌트를 미리보기 위한 함수들
//    - 개발 중 UI 변경 사항을 빠르게 확인하는 데 사용
// ==============================================================================================
@Preview(showBackground = true)
@Composable
fun PreviewMapScreen() {
    IssueissyuTheme {
        MapScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPinSummaryCard() {
    IssueissyuTheme {
        Column {
            PinSummaryCard(
                pin = MapPin(
                    id = "1",
                    title = "침수된 도로",
                    category = PinCategory.ISSUE, // ISSUE 핀은 resolutionStatus 필요, 추후 반영
                    description = "어제 비로 도로 일부가 침수되어 통행이 어렵습니다. 우회해야 할 것 같습니다.",
                    locationName = "역삼동 테헤란로 123",
                    position = LatLng(0.0, 0.0)
                ),
                onDismiss = {},
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            PinSummaryCard(
                pin = MapPin(
                    id = "2",
                    title = "커뮤니티 회의",
                    category = PinCategory.COMMUNICATION,
                    description = "다음 주 월요일 저녁 7시, 주민 센터에서 다음 달 행사 논의를 위한 회의가 있습니다. 많은 참여 바랍니다.",
                    locationName = "강남구 주민센터",
                    position = LatLng(0.0, 0.0)
                ),
                onDismiss = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}