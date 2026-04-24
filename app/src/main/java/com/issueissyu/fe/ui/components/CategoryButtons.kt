package com.issueissyu.fe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.Shop
import com.issueissyu.fe.ui.theme.White
import com.issueissyu.fe.ui.theme.Gray_8
import com.issueissyu.fe.ui.theme.suiteFontFamily

data class CategoryItem(
    val name: String,
    val icon: Int,
    val iconColor: Color,
    val backgroundColor: Color
)

@Composable
fun CategoryButtons(
    categories: List<CategoryItem>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier
                .weight(1f)
                .background(Color.Transparent)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(categories) { category ->
                val isSelected = category.name == selectedCategory
                val chipShape = RoundedCornerShape(18.dp)

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onCategorySelected(if (isSelected) null else category.name)
                    },
                    label = {
                        Text(text = category.name, fontSize = 13.sp, fontFamily = suiteFontFamily, fontWeight = FontWeight.ExtraBold)
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = category.icon),
                            contentDescription = category.name,
                            tint = category.iconColor,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    shape = chipShape,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.onSecondary,
                        labelColor = MaterialTheme.colorScheme.onBackground,
                        iconColor = category.iconColor,
                        selectedContainerColor = Gray_8,
                        selectedLabelColor = White,
                        selectedLeadingIconColor = category.iconColor
                    ),
                    modifier = Modifier
                        .height(36.dp)
                        .bottomOnlyDropShadow(chipShape)
                )
            }
        }
        onNotificationClick?.let { onClick ->
            IconButton(onClick = onClick) {
                Icon(
                    painter = painterResource(id = R.drawable.notification),
                    contentDescription = "Notification",
                    tint = Color.Unspecified
                )
            }
        }
    }
}

fun Modifier.bottomOnlyDropShadow(
    shape: RoundedCornerShape = RoundedCornerShape(18.dp)
): Modifier = this.dropShadow(
    shape = shape,
    shadow = Shadow(
        radius = 8.dp,
        spread = (-5).dp,
        color = Color.Black.copy(alpha = 0.3f),
        offset = DpOffset(0.dp, 5.dp)
    )
)

@Preview(showBackground = true)
@Composable
fun PreviewCategoryButtons() {
    val errorContainer = MaterialTheme.colorScheme.errorContainer
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer

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
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    CategoryButtons(
        categories = sampleCategories,
        selectedCategory = selectedCategory,
        onCategorySelected = { selectedCategory = it },
        onNotificationClick = { print("hello") }
    )
}
