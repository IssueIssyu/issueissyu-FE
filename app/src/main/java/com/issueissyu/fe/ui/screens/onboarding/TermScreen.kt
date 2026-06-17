// TermsAgreementScreen.kt
package com.issueissyu.fe.ui.screens.onboarding

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.components.CommonButton
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Text
import com.issueissyu.fe.ui.theme.suiteFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermScreen(
    onAgreeClick: () -> Unit = {},
    onTermsDetailClick: (TermsType) -> Unit = {},
    viewModel: TermViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onMarketingAgreementChanged(granted)
        onAgreeClick()
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions.values.any { it }
        val needNotification = viewModel.wasMarketingChecked()
        viewModel.onLocationAgreementChanged(granted)
        if (needNotification && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onAgreeClick()
        }
    }

    LaunchedEffect(uiState.submitError) {
        uiState.submitError?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.consumeSubmitError()
        }
    }

    Scaffold(
        topBar = {
            IssueissyuTopAppBar()
        },
        bottomBar = {
            CommonButton(
                onClick = {
                    viewModel.submitTerms(
                        onAgreeClick = onAgreeClick,
                        requestLocation = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                ),
                            )
                        },
                        requestNotification = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS,
                                )
                            } else {
                                onAgreeClick()
                            }
                        },
                    )
                },
                modifier =Modifier
                    .fillMaxWidth()
                    .padding(31.dp, 45.dp),
                text = "동의",
                isEnabled = uiState.isServiceAgreed && uiState.isPrivacyAgreed && !uiState.isSubmitting
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 31.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Logo
            Image(
                painterResource(R.drawable.img_logo_circle),
                contentDescription = "로고",
                modifier = Modifier.size(130.dp)

            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "환영합니다!",
                    style = IssueTypo.Bold18.copy(fontSize = 22.sp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "편리한 이슈있슈 서비스 이용을 위해\n약관에 동의해주세요",
                    style = IssueTypo.Regular16.copy(color = Gray_5),
                    textAlign = TextAlign.Center
                )

            }

            //전체 동의
            TermsCheckboxItem(
                text = "전체 동의",
                isChecked = uiState.isAllAgreed,
                onCheckedChange = { checked ->
                    viewModel.onAllAgreementChanged(checked)
                },
                labelType = null,
                showArrow = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Gray_3,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp)
            )

            // Individual Terms
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TermsCheckboxItem(
                    text = "서비스 이용약관 동의",
                    isChecked = uiState.isServiceAgreed,
                    onCheckedChange = { viewModel.onServiceChanged(it) },
                    labelType = LabelType.REQUIRED,
                    onArrowClick = { onTermsDetailClick(TermsType.SERVICE) }
                )

                TermsCheckboxItem(
                    text = "개인정보 수집 및 이용 동의",
                    isChecked = uiState.isPrivacyAgreed,
                    onCheckedChange = { viewModel.onPrivacyChanged(it) },
                    labelType = LabelType.REQUIRED,
                    onArrowClick = { onTermsDetailClick(TermsType.PRIVACY) }
                )

                TermsCheckboxItem(
                    text = "위치기반 서비스 이용약관 동의",
                    isChecked = uiState.isLocationAgreed,
                    onCheckedChange = { checked ->
                        viewModel.onLocationAgreementChanged(checked)
                    },
                    labelType = LabelType.OPTIONAL,
                    onArrowClick = { onTermsDetailClick(TermsType.LOCATION) }
                )

                TermsCheckboxItem(
                    text = "푸시 알림 수신 동의",
                    isChecked = uiState.isMarketingAgreed,
                    onCheckedChange = { checked ->
                        viewModel.onMarketingAgreementChanged(checked)
                    },
                    labelType = LabelType.OPTIONAL,
                    showArrow = false
                )
            }
        }
    }
}

//약관 동의 체크 박스
@Composable
fun TermsCheckboxItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    labelType: LabelType?,
    showArrow: Boolean = true,
    onArrowClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = BrandColor,
                uncheckedColor = Gray_5
            )
        )

        Spacer(modifier = Modifier.width(8.dp))


        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            labelType?.let {
                val labelText = when(it){
                    LabelType.REQUIRED -> "(필수)"
                    LabelType.OPTIONAL -> "(선택)"
                }
                val labelColor = when(it){
                    LabelType.REQUIRED -> Issue
                    LabelType.OPTIONAL -> Gray_5
                }

                Text(
                    text = "$labelText ",
                    fontFamily = suiteFontFamily,
                    fontWeight= FontWeight.Medium,
                    fontSize = 16.sp,
                    color = labelColor
                    )
            }

            Text(
                text = text,
                fontFamily = suiteFontFamily,
                fontSize = if(labelType == null) 18.sp else 14.sp,
                fontWeight = if(labelType == null) FontWeight.ExtraBold else FontWeight.Bold,
                color = Text
            )
        }

        // Arrow Icon
        if (showArrow && labelType != null) {
            IconButton(onClick = onArrowClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "상세보기",
                    tint = Color(0xFF999999)
                )
            }
        }
    }
}

enum class LabelType {
    REQUIRED, OPTIONAL
}

enum class TermsType {
    SERVICE, PRIVACY, LOCATION
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TermScreenPreview(){
    TermScreen()
}