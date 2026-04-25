package com.issueissyu.fe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.issueissyu.fe.ui.theme.Gray_4
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White
import com.issueissyu.fe.ui.theme.suiteFontFamily

@Composable
fun CommonTextField(
    value: String = "",
    onValueChange: (String) -> Unit,
    label: String = "",
    placeholder: String = "",   //더 사용성이 괜찮은 것 같아서 넣어봤습니다
    modifier: Modifier = Modifier,
    maxLines: Int =1,
    maxLength: Int = Int.MAX_VALUE,
    fontWeight: FontWeight = FontWeight.Black,
){
    Column(modifier=modifier){
        if(label.isNotEmpty()){
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = suiteFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    color = Title
                ),
                modifier =  Modifier.padding(start=10.dp, bottom =10.dp)
            )
        }
        BasicTextField(
            value = value,
            onValueChange = { newValue -> if (newValue.length <= maxLength) onValueChange(newValue)},
            maxLines = maxLines,
            textStyle = TextStyle(
                fontFamily = suiteFontFamily,
                fontWeight = fontWeight,  // 흠 요거 별칭 만들어 둬도 좋을 것 같아요
                fontSize = 18.sp,
                color = Title
            ),
            decorationBox = {
                innerTextField -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .background(White, RoundedCornerShape(15.dp))
                        .border(1.dp, Gray_4, RoundedCornerShape(15.dp))
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart
                ){
                    if (value.isEmpty() && placeholder.isNotEmpty()){
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontFamily = suiteFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 18.sp,
                                color = Gray_4
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
    var text by remember { mutableStateOf("") }
    CommonTextField(
        value = text,
        onValueChange = {text=it},
        maxLength = 12,
        label = "아이디",
        placeholder = "아이디를 입력하세요."
    )
}