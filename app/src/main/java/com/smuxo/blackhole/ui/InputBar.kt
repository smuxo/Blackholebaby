package com.smuxo.blackhole.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InputBar(onSend: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xEE111122))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Скажи что-нибудь...", fontSize = 13.sp) }
        )
        Button(onClick = {
            if (inputText.isNotBlank()) {
                onSend(inputText)
                inputText = ""
            }
        }) {
            Text("Отпр")
        }
    }
}
