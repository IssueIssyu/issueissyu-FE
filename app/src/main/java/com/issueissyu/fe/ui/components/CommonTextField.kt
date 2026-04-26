package com.issueissyu.fe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.issueissyu.fe.ui.theme.Gray_4
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White

@Composable
fun CommonTextField(
    value: String = "",
    onValueChange: (String) -> Unit,
    label: String = "",
    placeholder: String = "",
    modifier: Modifier = Modifier,
    maxLines: Int =1,
    maxLength: Int = Int.MAX_VALUE,
    textStyle: TextStyle = IssueTypo.Bold18,
    isPassword: Boolean = false,
    boxHeight: Dp = 55.dp
){
    Column(modifier=modifier){
        if(label.isNotEmpty()){
            Text(
                text = label,
                style = IssueTypo.Regular18.copy(color = Title),
                modifier =  Modifier.padding(start=10.dp, bottom =10.dp)
            )
        }
        BasicTextField(
            value = value,
            onValueChange = { newValue -> if (newValue.length <= maxLength) onValueChange(newValue)},
            maxLines = maxLines,
            textStyle = textStyle.copy(color = Title),
            visualTransformation = if(isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            decorationBox = {
                innerTextField -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(boxHeight)
                        .background(White, RoundedCornerShape(15.dp))
                        .border(1.dp, Gray_4, RoundedCornerShape(15.dp))
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart
                ){
                    if (value.isEmpty() && placeholder.isNotEmpty()){
                        Text(
                            text = placeholder,
                            style = textStyle.copy(
                                color = Gray_4,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommonTextFieldPreview(){
    var id by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Column() {
        CommonTextField(
            value = id,
            onValueChange = { id = it },
            maxLength = 12,
            label = "아이디",
            placeholder = "아이디를 입력하세요."
        )
        Spacer(modifier = Modifier.height(20.dp))
        CommonTextField(
            value = content,
            onValueChange = { content = it },
            maxLength = 12,
            label = "상세 설명",
            placeholder = "상세 설명을 작성해 주세요.\n\n한 줄을 적고\n해시태그를 눌러\n이슈있슈 AI로 빠르게\n원하는 말투로 글을 작성할 수 있어요!",
            maxLines = 10,
            textStyle = IssueTypo.Regular16,
            boxHeight = 152.dp
        )
    }
}