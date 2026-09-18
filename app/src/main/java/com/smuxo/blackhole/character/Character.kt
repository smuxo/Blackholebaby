package com.smuxo.blackhole.character

import androidx.compose.runtime.mutableStateOf
import kotlin.math.abs

class Character(
    var x: Float = 0f,
    var y: Float = 0f
) {
    var targetX: Float = x
    var targetY: Float = y
    var moving: Boolean = false
    val animation = mutableStateOf(DyrAnimation.IDLE)
    val speech = mutableStateOf<String?>(null)
    var onPositionChanged: ((Float, Float) -> Unit)? = null

    fun setPosition(px: Float, py: Float) {
        x = px
        y = py
        targetX = px
        targetY = py
        onPositionChanged?.invoke(x, y)
    }

    fun moveTo(px: Float, py: Float) {
        targetX = px
        targetY = py
        moving = true
    }

    fun step(): Boolean {
        if (!moving) return false
        val dx = targetX - x
        val dy = targetY - y
        if (abs(dx) < 2f && abs(dy) < 2f) {
            x = targetX
            y = targetY
            moving = false
            onPositionChanged?.invoke(x, y)
            playAnimation(DyrAnimation.IDLE)
            return true
        }
        x += dx * 0.15f
        y += dy * 0.15f
        onPositionChanged?.invoke(x, y)
        return true
    }

    fun say(text: String) {
        speech.value = text
    }

    fun clearSpeech() {
        speech.value = null
    }

    fun playAnimation(anim: DyrAnimation) {
        animation.value = anim
    }
}
