package com.issueissyu.fe.ui.screens.mypage

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.components.CommonButton
import com.issueissyu.fe.ui.components.CommonTextField
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_2
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Text
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White


@Composable
fun ProfileChangeScreen(
    onBackClick: () -> Unit,
    onCollectionClick: () -> Unit,
    onCompleteClick: () -> Unit,
    viewModel: ProfileChangeViewModel = hiltViewModel()
){
    val currentNickname by viewModel.currentNickname.collectAsStateWithLifecycle()
    val inputNickname by viewModel.inputNickname.collectAsStateWithLifecycle()
    val isNicknameAvailable by viewModel.isNicknameAvailable.collectAsStateWithLifecycle()
    val isCheckingNickname by viewModel.isCheckingNickname.collectAsStateWithLifecycle()
    val isCompleteEnabled by viewModel.isCompleteEnabled.collectAsStateWithLifecycle()
    val isCheckButtonEnabled by viewModel.isCheckButtonEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.showToast.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .background(White)
    ){

        //상단 바
        IssueissyuTopAppBar(
            titleText = "프로필 편집",
            onBackClick = onBackClick
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(31.dp, 50.dp)
        ){
            //프로필 편집
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                //프로필 사진
                Image(
                    painter = painterResource(R.drawable.ic_fire),
                    contentDescription = "프로필 사진",
                    modifier = Modifier
                        .size(130.dp)
                        .background(White, CircleShape)
                        .clip(CircleShape)
                        .scale(1.5f),
                    contentScale = ContentScale.Crop,
                    alignment = BiasAlignment(
                        horizontalBias = 0f,     // 가로는 중앙
                        verticalBias = -0.3f     // 세로는 top과 center 중간
                    )
                )

                Spacer(modifier = Modifier.size(15.dp))

                //버튼
                Button(
                    onClick = onCollectionClick,
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White
                    ),
                    border = BorderStroke(1.dp, Gray_3)
                ){
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "내 컬렉션 보러가기",
                            style = IssueTypo.Regular16.copy(color = Title)
                        )

                        Spacer(modifier = Modifier.size(10.dp))

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "네비게이트",
                            tint = Gray_5,
                            modifier = Modifier
                                .size(20.dp)
                        )

                    }
                }
            }

            Spacer(modifier = Modifier.size(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ){
                //닉네임 재설정
                CommonTextField(
                    label = "닉네임 재설정",
                    value = inputNickname,
                    onValueChange = viewModel::onNicknameChange,
                )

                Spacer(modifier = Modifier.size(10.dp))

                Row(){
                    // 중복 확인 상태 메시지
                    if (isNicknameAvailable != null) {
                        Text(
                            text = if (isNicknameAvailable == true)
                                "사용 가능한 닉네임입니다"
                            else
                                "이미 사용 중인 닉네임입니다",
                            style = IssueTypo.Regular16.copy(
                                color = if (isNicknameAvailable == true) BrandColor else Issue
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp, start = 4.dp)
                        )
                    }

                    //중복 확인 버튼
                    Button(
                        onClick = viewModel::checkNicknameDuplicate,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if(isCheckButtonEnabled && !isCheckingNickname) BrandColor else Gray_2
                        ),
                        enabled = isCheckButtonEnabled && !isCheckingNickname
                    ){
                        Text(
                            text = if (isCheckingNickname) "확인 중..." else "중복 확인",
                            style = IssueTypo.Regular15.copy(color = if (isCheckButtonEnabled && !isCheckingNickname) White else Text)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            CommonButton(
                onClick = { viewModel.updateProfile(onSuccess = onCompleteClick) },
                text = "완료",
                modifier = Modifier
                    .fillMaxWidth(),
                isEnabled = isCompleteEnabled,
            )
        }
    }
}








@Preview(showBackground = true)
@Composable
fun PreviewProfileChangeScreen(){
    ProfileChangeScreen(
        onBackClick = {},
        onCollectionClick = {},
        onCompleteClick = {}
    )
}