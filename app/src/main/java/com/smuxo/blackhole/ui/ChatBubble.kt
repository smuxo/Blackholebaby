package com.smuxo.blackhole.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatBubble(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 13.sp,
        modifier = Modifier
            .widthIn(max = 220.dp)
            .background(Color(0xCC1A1A2E), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}
