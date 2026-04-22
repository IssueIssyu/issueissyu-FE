package com.issueissyu.fe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.Festival

data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val iconColor: Color,
    val backgroundColor: Color
)

@Composable
fun CategoryButtons(
    categories: List<CategoryItem>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .background(MaterialTheme.colorScheme.onSecondary)
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
                        imageVector = category.icon,
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
}

@Preview(showBackground = true)
@Composable
fun PreviewCategoryButtons() {
    val sampleCategories = listOf(
        CategoryItem("이슈", Icons.AutoMirrored.Filled.List, MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer),
        CategoryItem("가게", Icons.Default.ShoppingCart, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer),
        CategoryItem("축제", Icons.Default.Star, Festival, MaterialTheme.colorScheme.tertiaryContainer),
        CategoryItem("소통", Icons.Default.LocationOn, Communication, MaterialTheme.colorScheme.secondaryContainer)
    )
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    CategoryButtons(
        categories = sampleCategories,
        selectedCategory = selectedCategory,
        onCategorySelected = { selectedCategory = it }
    )
}