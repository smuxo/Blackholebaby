package com.smuxo.blackhole.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.smuxo.blackhole.character.Character
import com.smuxo.blackhole.character.SpriteAnimator
import kotlinx.coroutines.delay

@Composable
fun CharacterContent(
    character: Character,
    animator: SpriteAnimator,
    onTap: () -> Unit
) {
    var frameIndex by remember { mutableStateOf(0) }
    val currentAnimation = character.animation.value
    val currentSpeech = character.speech.value

    LaunchedEffect(currentAnimation) {
        frameIndex = 0
        val count = animator.frameCount(currentAnimation)
        while (true) {
            delay(120L)
            frameIndex = (frameIndex + 1) % count
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (currentSpeech != null) {
            ChatBubble(currentSpeech)
            Spacer(modifier = Modifier.padding(2.dp))
        }
        val frameBitmap = animator.frameAt(currentAnimation, frameIndex)
        Image(
            bitmap = frameBitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .clickable { onTap() }
        )
    }
}
