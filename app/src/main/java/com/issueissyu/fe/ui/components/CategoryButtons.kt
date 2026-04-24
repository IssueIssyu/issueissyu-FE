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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.Shop


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

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onCategorySelected(if (isSelected) null else category.name)
                    },
                    label = {
                        Text(text = category.name, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = category.icon),
                            contentDescription = category.name,
                            tint = category.iconColor,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.onSecondary,
                        labelColor = MaterialTheme.colorScheme.onBackground,
                        iconColor = category.iconColor,
                        selectedContainerColor = category.backgroundColor,
                        selectedLabelColor = MaterialTheme.colorScheme.onBackground,
                        selectedLeadingIconColor = category.iconColor
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline,
                        selectedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.height(36.dp)
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

@Preview(showBackground = true)
@Composable
fun PreviewCategoryButtons() {
    val sampleCategories = listOf(
        CategoryItem("이슈", R.drawable.issue, Issue, MaterialTheme.colorScheme.errorContainer),
        CategoryItem("소통", R.drawable.communicate, Communication, MaterialTheme.colorScheme.secondaryContainer),
        CategoryItem("가게", R.drawable.shop, Shop, MaterialTheme.colorScheme.primaryContainer),
        CategoryItem("축제", R.drawable.festival, Festival, MaterialTheme.colorScheme.tertiaryContainer)
    )
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    CategoryButtons(
        categories = sampleCategories,
        selectedCategory = selectedCategory,
        onCategorySelected = { selectedCategory = it },
        onNotificationClick = { /* 알림 클릭 시 동작 */ }
    )
}