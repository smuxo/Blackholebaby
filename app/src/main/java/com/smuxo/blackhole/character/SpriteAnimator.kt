package com.smuxo.blackhole.character

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

enum class DyrAnimation(val row: Int) {
    IDLE(0),
    RUN(1),
    JUMP(2),
    SLEEP(3),
    HAPPY(4),
    SURPRISE(5),
    ACTION(6),
    CELEBRATE(7);

    companion object {
        fun fromKey(key: String): DyrAnimation {
            return when (key.lowercase()) {
                "idle" -> IDLE
                "run" -> RUN
                "jump" -> JUMP
                "sleep" -> SLEEP
                "happy" -> HAPPY
                "surprise" -> SURPRISE
                "action" -> ACTION
                "celebrate" -> CELEBRATE
                else -> IDLE
            }
        }
    }
}

class SpriteAnimator(
    context: Context,
    assetName: String = "spritesheet.png",
    private val columns: Int = 6,
    private val rows: Int = 8
) {
    private val frames: Array<Array<Bitmap>>
    val frameWidth: Int
    val frameHeight: Int

    init {
        val options = BitmapFactory.Options()
        options.inScaled = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inDither = false
        val sheet = context.assets.open(assetName).use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IllegalStateException("spritesheet decode failed")
        val safeSheet = if (sheet.config != Bitmap.Config.ARGB_8888) {
            sheet.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            sheet
        }
        frameWidth = safeSheet.width / columns
        frameHeight = safeSheet.height / rows
        frames = Array(rows) { rowIndex ->
            Array(columns) { colIndex ->
                Bitmap.createBitmap(
                    safeSheet,
                    colIndex * frameWidth,
                    rowIndex * frameHeight,
                    frameWidth,
                    frameHeight
                )
            }
        }
    }

    fun frameCount(animation: DyrAnimation): Int = frames[animation.row].size

    fun frameAt(animation: DyrAnimation, index: Int): Bitmap {
        val row = frames[animation.row]
        return row[index % row.size]
    }
}
