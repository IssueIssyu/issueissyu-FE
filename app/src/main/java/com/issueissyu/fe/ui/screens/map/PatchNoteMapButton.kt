package com.issueissyu.fe.ui.screens.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import com.issueissyu.fe.R

@Composable
fun PatchNoteMapButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.patchnote),
        contentDescription = "패치노트",
        modifier = modifier
            .width(72.dp)
            .height(81.dp)
            .clickable(onClick = onClick),
        contentScale = ContentScale.Fit
    )
}
