package com.issueissyu.fe.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.CommunicationContainerLight
import com.issueissyu.fe.ui.theme.White
import com.issueissyu.fe.ui.theme.Gray_1
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueContainer
import com.issueissyu.fe.ui.theme.IssueContainerLight
import com.issueissyu.fe.ui.theme.Orange
import com.issueissyu.fe.ui.theme.suiteFontFamily

@Composable
fun CommonButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true

){
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandColor,
            contentColor = Gray_1,
            disabledContainerColor = Gray_3,
            disabledContentColor = Gray_5
        ),
        contentPadding = PaddingValues(18.dp)
    ){
        Text(
            text = text,
            fontFamily = suiteFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            //typography 쓰는 방안 고려
        )
    }
}

// 로그인은 네이버/카카오에서 지정한 이미지를 사용할 예정

//수정 버튼
enum class BtnSize(val btn: Dp, val icon: Dp){
    Large(36.dp, 36.dp * 0.56f),
    Small(22.dp, 22.dp * 0.56f),
}

@Composable
private fun CircleIconButton(
    iconRes: Int,
    contentDescription: String,
    size: BtnSize,
    onClick: () -> Unit,
    containerColor: Color = White,
){
    Box(
        modifier = Modifier
            .size(size.btn)
            .shadow(
                elevation =3.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ){
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(size.icon),
        )
    }
}

@Composable
fun EditButton(
    isAuthor: Boolean,
    isEditing: Boolean,
    size: BtnSize = BtnSize.Large,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    onReportClick: () -> Unit,
){
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
        when{
            isAuthor && !isEditing -> {
                CircleIconButton(
                    iconRes = R.drawable.ic_edit,
                    contentDescription = "수정",
                    size = size,
                    onClick = onEditClick
                )
                CircleIconButton(
                    iconRes = R.drawable.ic_delete,
                    contentDescription = "삭제",
                    size = size,
                    onClick = onDeleteClick
                )
            }
            isAuthor && isEditing -> {
                CircleIconButton(
                    iconRes = R.drawable.ic_cancel,
                    contentDescription = "취소",
                    size = size,
                    onClick = onCancelClick
                )
                CircleIconButton(
                    iconRes = R.drawable.ic_edit_complete,
                    contentDescription = "저장",
                    size = size,
                    onClick = onSaveClick,
                    containerColor = BrandColor
                )
            }
            else -> {
                CircleIconButton(
                    iconRes = R.drawable.ic_report,
                    contentDescription = "신고",
                    size = size,
                    onClick = onReportClick
                )
            }
        }
    }
}

//지금 가요 버튼

enum class ActionState(
    val label: String,
    val iconRes: Int,
    val bgColor: Color,
    val textColor: Color
){
    DEFAULT("지금 가요", R.drawable.ic_fire, Gray_1, Issue),
    MOVING("이동중", R.drawable.ic_moving, IssueContainerLight, Orange),
    DONE("참여 완료", R.drawable.ic_complete, CommunicationContainerLight, BrandColor)
}

@Composable
fun GoNowButton(
    state: ActionState = ActionState.DEFAULT,
    onClick: () -> Unit
){
    Button(
        onClick = onClick,
        enabled = state == ActionState.DEFAULT,
        modifier = Modifier
            .width(180.dp)  //fillmaxwidth쓸 수 있는지 검토 필요
            .height(52.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = state.bgColor,
            disabledContainerColor = state.bgColor
        ),
        ){
        Image(
            painter = painterResource(state.iconRes),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = state.label,
            fontFamily = suiteFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = state.textColor,
        )
    }
}


@Composable
fun SignButton(
    isSigned: Boolean = false,
    count: Int,
    onClick: () -> Unit,
){
    Button(
        onClick = onClick,
        enabled = !isSigned,
        modifier = Modifier
            .width(180.dp)
            .height(52.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Orange,
            disabledContentColor = IssueContainer
        )
    ){
        Image(
            painter = painterResource(R.drawable.ic_notification),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "청원",
            fontFamily = suiteFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = White
        )
    }
}



@Preview(showBackground = true)
@Composable
fun ButtonPreview(){
    Column(){
        CommonButton(
            text = "동의",
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
        CommonButton(
            text = "작성 완료",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            isEnabled = false)

        EditButton(
            isAuthor = true,
            isEditing = false,
            onEditClick = {},
            onDeleteClick = {},
            onCancelClick = {},
            onSaveClick = {},
            onReportClick = {},
            size = BtnSize.Small
        )
        EditButton(
            isAuthor = true,
            isEditing = false,
            onEditClick = {},
            onDeleteClick = {},
            onCancelClick = {},
            onSaveClick = {},
            onReportClick = {}
        )
        EditButton(
            isAuthor = true,
            isEditing = true,
            onEditClick = {},
            onDeleteClick = {},
            onCancelClick = {},
            onSaveClick = {},
            onReportClick = {},
        )
        EditButton(
            isAuthor = false,
            isEditing = false,
            onEditClick = {},
            onDeleteClick = {},
            onCancelClick = {},
            onSaveClick = {},
            onReportClick = {}
        )
        GoNowButton(onClick = { })
        GoNowButton(onClick = { }, state = ActionState.MOVING)
        GoNowButton(onClick = { }, state = ActionState.DONE)
        SignButton(count = 0, onClick = { })
    }
}